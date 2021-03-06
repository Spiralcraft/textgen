//
// Copyright (c) 1998,2007 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.textgen;

import spiralcraft.lang.Binding;
import spiralcraft.lang.Context;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

import spiralcraft.builder.Assembly;
import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.Lifecycler;
import spiralcraft.common.declare.DeclarationInfo;


import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.textgen.compiler.DefineUnit;
import spiralcraft.textgen.compiler.TglUnit;

import spiralcraft.text.ParsePosition;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.text.xml.Attribute;
import spiralcraft.util.ListMap;

import spiralcraft.app.Component;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.Event;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandler;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.Parent;
import spiralcraft.app.Scaffold;
import spiralcraft.app.State;
import spiralcraft.app.kit.StandardMessageHandlerChain;
import spiralcraft.app.kit.TraceHandler;


import java.net.URI;


/**
 * <p>A compositional unit of a TGL document.
 * </p>
 * 
 * <p>An Element may contain other Elements and/or content, forming a tree of
 *   Elements which generate output.
 * </p>
 *   
 * <p>Elements are thread-safe, and thus must not maintain dynamic 
 *   state internally. An element resolves its state within the render()
 *   or message() methods, and uses the EventContext object to provide
 *   a reference its state.
 * </p>
 *   
 * <p>The Elements associate with application specific runtime context via the
 *   Focus, which is provided to the root Element by the container. Elements may
 *   extend this Focus to refine the context for their child elements. The
 *   spiralcraft.lang expression language is used to access application context
 *   through this Focus.
 * </p>
 *   
 * <p>Elements are beans which are instantiated and configured 
 *   (ie. parameterized) using the spiralcraft.builder package. Each Element
 *   is associated with an Assembly. The Assembly provides a means for 
 *   Elements to associate with other elements in the Element structure
 *   without those elements being adjacent to each other.
 * </p>
 * 
 * <p>Elements are created by first instantiating them and applying the bean
 *   properties specified in their TGL declarations. The Element is then bound
 *   to its already bound parent Element, where it is able to resolve any
 *   expressions by binding them to the Focus provided by its parent element.
 * </p> 
 */
public class Element
  implements Component,Parent
{ 

  private Component[] children;
  
  private Parent parent;
  private Scaffold<?> scaffold;
  
  private Assembly<?> assembly;
  private String id;
  private MessageHandlerChain handlerChain;
  protected boolean debug;
  private ParsePosition position;
  
  protected final ClassLog log=ClassLog.getInstance(getClass());
  protected Level logLevel=Level.INFO;
  
  protected DefineUnit skin;
  protected URI focusURI;
  
  protected Context innerContext;
  protected boolean addSelfFacet;

  private LinkedList<Contextual> parentContextuals;
  private LinkedList<Contextual> exportContextuals;
  private LinkedList<Contextual> selfContextuals;  
  
  private Focus<?> selfFocus;
  
  private HashSet<Message.Type> notifications;
  private HashSet<Message.Type> subscriptions;
  private ListMap<Message.Type,Integer> childSubscriptions;
  private boolean ignoreBindError;
  private ContextualException bindError;
  private Binding<?> onBind;
  
  private HashMap<String,Integer> idMap;
  private DeclarationInfo declarationInfo;
  protected boolean alwaysRunHandlers;
  
  class DefaultHandler
    implements MessageHandler
  {    
    @Override
    public Focus<?> bind(
      Focus<?> focusChain)
      throws BindException
    { return focusChain;
    }

    @Override
    public void handleMessage(
      Dispatcher context,
      Message message,
      MessageHandlerChain next)
    { relayMessage(context,message);
    }
  }
  
//  public Element(Element parent,Scaffold<?,Element,?> scaffold)
//  { 
//    this.parent=parent;
//    this.scaffold=scaffold;
//  }
  
  public Element()
  {
  }
  
  @Override
  public Reflector<Component> reflect()
  { return BeanReflector.getInstance(getClass());
  }
  
  public void setFocusURI(URI focusURI)
  { this.focusURI=focusURI;
  }
  
  public void setCodePosition(ParsePosition position)
  { this.position=position.clone();
  }
  
  public ParsePosition getCodePosition()
  { return this.position!=null?this.position:(scaffold!=null?scaffold.getPosition():null);
  }
  
  public void setSkin(DefineUnit skin)
  { this.skin=skin;
  }


  public void setIgnoreBindError(boolean ignoreBindError)
  { this.ignoreBindError=ignoreBindError;
  }
  
  @Override
  public Message.Type[] getSubscribedTypes()
  { 
    if (subscriptions!=null)
    { return subscriptions.toArray(new Message.Type[subscriptions.size()]);
    }
    else
    { return null;
    }
  }
  
  /**
   * @see Parent.subscribe()
   */
  @Override
  public void subscribe(Message.Type[] types)
  {
    
    for (Message.Type type: types)
    {
      if (notifications==null 
          || !notifications.contains(type)
          )
      { 
        if (subscriptions==null)
        { subscriptions=new HashSet<Message.Type>();
        }  
        subscriptions.add(type);
      }
    }
  }
  
  public void setOnBind(Binding<?> onBind)
  { 
    removeSelfContextual(this.onBind);
    this.onBind=onBind;
    addSelfContextual(this.onBind);
  }
  
  /**
   * <p>Add a Contextual to be bound to this Control's parent's context.
   * </p>
   * 
   * <p>The Focus returned by the Contextual will not be used by this
   *   component.
   * </p>
   * 
   * @param contextual
   */
  protected final void addParentContextual(Contextual contextual)
  { 
    if (this.parentContextuals==null)
    { this.parentContextuals=new LinkedList<Contextual>();
    }
    this.parentContextuals.add(contextual);
  }

  /**
   * <p>Remove a Contextual from the list of Contextuals to be bound
   * </p>
   * 
   * @param contextual
   */
  protected final void removeParentContextual(Contextual contextual)
  {
    if (this.parentContextuals!=null)
    { this.parentContextuals.remove(contextual);
    }
  }
  
  protected final void bindParentContextuals(Focus<?> focus)
    throws ContextualException
  { bindContextuals(focus,parentContextuals);
  }
  
  /**
   * <p>Add a Contextual to be bound to this Control's target's context 
   * </p>
   * 
   * <p>The Focus returned by the Contextual will not be used by this
   *   component.
   * </p>
   *
   * @param contextual
   */
  protected void addExportContextual(Contextual contextual)
  { 
    if (this.exportContextuals==null)
    { this.exportContextuals=new LinkedList<Contextual>();
    }
    this.exportContextuals.add(contextual);
  }

  /**
   * <p>Remove a Contextual from the list of Contextuals to be bound
   * </p>
   * 
   * @param contextual
   */
  protected void removeExportContextual(Contextual contextual)
  {
    if (this.exportContextuals!=null)
    { this.exportContextuals.remove(contextual);
    }
  }

  protected final void bindExportContextuals(Focus<?> focus)
      throws ContextualException
  { bindContextuals(focus,exportContextuals);
  }

  /**
   * <p>Add a Contextual to be bound to this Control's own context 
   * </p>
   * 
   * <p>The Focus returned by the Contextual will not be used by this
   *   component.
   * </p>
   * 
   * @param contextual
   */
  protected void addSelfContextual(Contextual contextual)
  { 
    if (this.selfContextuals==null)
    { this.selfContextuals=new LinkedList<Contextual>();
    }
    this.selfContextuals.add(contextual);
  }
  
  
  /**
   * </p>Remove a Contextual from the list of Contextuals to be bound
   * <p>
   * 
   * @param contextual
   */
  protected void removeSelfContextual(Contextual contextual)
  {
    if (this.selfContextuals!=null)
    { this.selfContextuals.remove(contextual);
    }
  }
  
  protected final void bindSelfContextuals(Focus<?> focus)
      throws ContextualException
  { bindContextuals(focus,selfContextuals);
  }

  
  protected final void bindContextuals
    (Focus<?> focus,List<Contextual> contextuals)
    throws ContextualException
  { 
    if (contextuals!=null)
    {
      for (Contextual contextual:contextuals)
      { contextual.bind(focus);
      }
    }
  }  
  
  /**
   * <p>Called to log an otherwise handled exception consumed during processing
   * </p>
   * 
   * @param x
   */
  protected void logHandledException(Dispatcher context,Throwable x)
  { log.log(Level.INFO,getLogPrefix(context)+": Caught handled exception ",x);
  }

  /**
   * <p>Override to incorporate contextual information from the EventContext
   *   into log messages
   * </p>
   * @return Any contextual information derived from the EventContext, or ""
   *   (default)
   */
  protected String getLogPrefix(Dispatcher context)
  { 
    String logPrefix=context.getContextInfo();
    return (logPrefix!=null?logPrefix:"")+" "+getCodePosition();
  }
  
  protected String getErrorContext()
  { return " "+getCodePosition().toString()+(id!=null?" id=["+id+"]: ":" ");
  }
  
  @Override
  public String toString()
  { return super.toString()+getErrorContext();
  }
  // private int[] path;

  /**
   * Specify an id for this Element and make it visible to the expression
   *   language when used anywhere inside this element. 
   */
  public void setId(String id)
  { this.id=id;
  }
  
  @Override
  public String getId()
  { return id;
  }
  
  public void setDebug(boolean debug)
  { 
    this.debug=debug;
    if (debug)
    { this.logLevel=Level.FINE;
    }
    else
    { this.logLevel=Level.INFO;
    }
  }
  
  public boolean isDebug()
  { return debug;
  }

  @Override
  public void setLogLevel(Level logLevel)
  { 
    this.logLevel=logLevel;
    if (logLevel.isTrace())
    { addHandler(new TraceHandler());
    }
    if (logLevel.isDebug())
    { this.debug=true;
    }
    else
    { this.debug=false;
    }
  }

  protected synchronized void addHandler(MessageHandler handler)
  { 
    if (handlerChain==null)
    { handlerChain=new StandardMessageHandlerChain(handler);
    }
    else
    { handlerChain.chain(handler);
    }
  }
  
  protected Component getChild(int i)
  { 
    if (children==null)
    { return null;
    }
    return children[i];
  }
  
//  /**
//   * @return The Focus associated with this Element. Defaults to the parent
//   *   Element's Focus, unless overridden.
//   */
//  protected Focus<?> getFocus()
//  { 
//    if (parent!=null)
//    { return parent.getFocus();
//    }
//    System.err.println("Element: "+getClass().getName()+" no parent, no focus");
//    return null;
//  }
  
  /**
   * @return The context URI associated with this Element, which is generally
   *   the URI of the directory that contains the TGL source file. 
   */
  public URI getContextURI()
  {
    if (parent!=null)
    { return ((Element) parent).getContextURI();
    }
    return null;
  }

  /**
   * @return The Assembly from which this Element was instantiated
   */
  protected Assembly<?> getAssembly()
  { return assembly;
  }
  
  /**
   * Specify the Assembly from which this Element was instantiated. Can be
   *   use to publish this Element itself into the Focus hierarchy.   
   */
  public void setAssembly(Assembly<?> assembly)
  { this.assembly=assembly;
  }
  
  public boolean hasChildren()
  { return children!=null && children.length>0;
  }
  
  /**
   * <p>Specify the Element that contains this Element. This is always called 
   *   by the framework before bind() is called.
   * </p>
   * 
   */
  @Override
  public void setParent(Parent parent)
  { 
    // XXX: Maybe ok to set the same parent twice- eval on a case basis
    //   
    if (this.parent!=null)
    { throw new IllegalStateException("Parent already specified");
    }
    this.parent=parent;    
  }
  
  /**
   * <p>Specify the Scaffold responsible for defining this Element and
   *   its children
   * </p>
   * 
   */
  @Override
  public void setScaffold(Scaffold<?> scaffold)
  { 
    if (this.scaffold!=null)
    { throw new IllegalStateException("Scaffold already specified");
    }
    this.scaffold=scaffold;
  }
  
  public Scaffold<?> getScaffold()
  { return scaffold;
  }
  
  @Override
  public Parent getParent()
  { return parent;
  }
  
  
  @Override
  public final Focus<?> bind(Focus<?> focus)
    throws ContextualException
  {
    if (logLevel.isDebug())
    { log.fine(getDeclarationInfo()+": Binding...");
    }
    try
    { return bindStandard(focus);
    }
    catch (ContextualException x)
    {
      bindError=x;
      if (!ignoreBindError)
      { throw new ContextualException("Bind error",this.getErrorContext(),x);
      }
      else
      { 
        log.info
          ("Ignoring bind error at "
            +this.getErrorContext()+": "+x.toString()
          );
        return focus;
      }
    }
    finally
    {
      if (logLevel.isDebug())
      { log.fine(getDeclarationInfo()+": Bound");
      }
    }
  }
  
  /**
   * <p>Called when binding Units, to allow the Element to initialize by 
   *   referencing its data source and parent Element, supplied before this
   *   method is called. 
   * </p>
   *
   *  <p>
   *    This method should call TglUnit.bind() on its child units at an
   *    appropriate time. The default behavior is to bind all the child units.
   *  </p>
   * @throws BindException 
   * @throws MarkupException
   */
  protected Focus<?> bindStandard(Focus<?> focus)
    throws ContextualException
  { 
    bindParentContextuals(focus);
    
    
    // Focus parentFocus=focus;
    // focus=bindImports()
    //
    // if (focus==parentFocus)
    // { focus=focus.chain(focus.getSubject());
    // }
    // 
    
    bindSelfFocus(focus);
    if (addSelfFacet)
    {
      focus=focus.chain(focus.getSubject());
      focus.addFacet(selfFocus);
    }
    
    bindHandlers(focus);
    
    bindExportContextuals(focus);
    
    bindChildren(focus);
    
    onBind(focus);
    bindComplete(focus);
    return focus;
    
  }
  
  
  protected void onBind(Focus<?> focus)
  {
    if (onBind!=null)
    { onBind.get();
    }
  }
  
  protected Focus<?> bindSelfFocus(Focus<?> focus) 
    throws ContextualException
  { 
    selfFocus=focus.chain
      (new SimpleChannel<Element>(Element.this,true));

    DeclarationInfo di=this.declarationInfo;
    while (di!=null)
    { 
      if (di.getDeclaredType()!=null)
      { selfFocus.addAlias(di.getDeclaredType());
      }
      di=di.getBase();
    }
    bindSelfContextuals(selfFocus);
    return selfFocus;
  }
  
  /**
   * Override to add any handlers after the parent and self context has been
   *   bound
   */
  protected void addHandlers()
    throws ContextualException
  {

  }
  
  protected final void bindHandlers(Focus<?> focus)
    throws ContextualException
  { 

    addHandlers();
    if (handlerChain!=null)
    { 
      handlerChain.chain(createDefaultHandler());
      handlerChain.bind(focus);
    }
    
  }
  
  /**
   * <p>Override to process any information received from descendants during
   *   the bind process
   * </p>
   * 
   * @param focus
   */
  protected void bindComplete(Focus<?> focus)
    throws ContextualException
  { 
  }
  
  protected MessageHandler createDefaultHandler()
  { return new DefaultHandler();
  }
  
  /**
   * <p>Binds the children provided by the scaffold, if any. Called by bind
   *   to descend the containership hierarchy
   * </p>
   * 
   * @param focus
   * @throws BindException
   * @throws MarkupException
   */
  @SuppressWarnings("unchecked")
  protected final void bindChildren(Focus<?> focus)
    throws ContextualException
  { 
    bindChildren
      (focus
      ,scaffold!=null
        ?(List<TglUnit>) scaffold.getChildren()
        :null
      );
  }
  
  public Component[] getChildren()
  { return children;
  }
  
  /**
   * Binds the specified children to this component. 
   * 
   * @param focus
   * @param childUnits
   * @throws MarkupException
   */
  protected final void bindChildren
    (Focus<?> focus,List<TglUnit> childUnits
    )
    throws ContextualException
  {
    
    if (innerContext!=null)
    { focus=innerContext.bind(focus);
    }
    
    if (skin!=null)
    {
      Component skinElement
        =skin.bindContent(new Attribute[0],focus,this,childUnits,((TglUnit) scaffold).getNamespaceResolver());
      children=new Component[1];
      children[0]=skinElement;
    }
    else
    {
      List<Scaffold<?>> scaffoldChildren=expandChildren(focus,childUnits);
      
      LinkedList<Component> childList=null;
      
      childList=addFirstBoundChildren(focus,childList);
      
      if (scaffoldChildren!=null)
      { 
        if (childList==null)
        { childList=new LinkedList<>();
        }
        
        int i=0;
        for (Scaffold<?> child: scaffoldChildren)
        { 
          Component childComponent=bindChild(i,child,focus);
          if (childComponent!=null)
          { 
            childList.add(childComponent);
            i++;
          }
        }
      }
      
      childList=addLastBoundChildren(focus,childList);
      
      if (childList!=null)
      { children=childList.toArray(new Component[childList.size()]);
      }
    }
    
    if (children!=null)
    {
      // Register children
      int index=0;
      for (Component child : children)
      { 
        // Add child subscriptions
        Message.Type[] types=child.getSubscribedTypes();
        if (types!=null)
        {
          if (childSubscriptions==null)
          { childSubscriptions=new ListMap<Message.Type,Integer>();
          }
          
          for (Message.Type type : types)
          { 
            if (subscriptions==null)
            { subscriptions=new HashSet<Message.Type>();
            }
            childSubscriptions.add(type,index);
            subscriptions.add(type);
          }
        }
        
        if (child.getId()!=null)
        { registerChild(child.getId(),index);
        }  
        index++;
      }
    }
    
  }

  /**
   * Override to generate components to add to the head of the child list
   * 
   * @param focus
   * @param collection
   * @return
   */
  protected LinkedList<Component> 
    addFirstBoundChildren(Focus<?> focus,LinkedList<Component> collection)
      throws ContextualException
  { return collection;
  }
  
  /**
   * Override to generate components and add them to the tail of the child list
   * @param focus
   * @param collection
   * @return
   */
  protected LinkedList<Component> 
    addLastBoundChildren(Focus<?> focus,LinkedList<Component> collection)
      throws ContextualException
  { return collection;
  }

  /**
   * Called for each Scaffold unit when generating this element's children
   * 
   * @param index
   * @param child
   * @param focus
   * @return
   * @throws ContextualException
   */
  protected Component bindChild(int index,Scaffold<?> child,Focus<?> focus)
    throws ContextualException
  { return child.bind(focus,this);
  }
  
  /**
   * <p>Called by bindChildren() before binding to expand the tree between
   *   this node and the specified children.
   * </p>
   * 
   * <p>This is where layout components, default views, or other automatically
   *   inserted structure is applied
   * </p>
   * 
   * <p>The default behavior does not alter the predefined children
   * </p>
   * 
   * @param focus
   * @param childUnits
   * @return
   */
  protected List<Scaffold<?>> expandChildren
    (Focus<?> focus,List<TglUnit> childUnits)
    throws ContextualException
  { 
    if (childUnits==null)
    { return null;
    }
    List<Scaffold<?>> children=new LinkedList<Scaffold<?>>();
    for (Scaffold<?> unit:childUnits)
    { children.add(unit);
    }
    return children;
  }
  


  /**
   * <p>Recursively send a message to one or more components in the tree, to
   *   provide an opportunity for them to update their state or any of their
   *   bindings.
   * </p>
   * 
   * <p>If a message is intended for a particular Element in the tree, the
   *   first element of the path list will be the index of this element's
   *   child to forward the message to. 
   * </p>
   * 
   * <p>Message.isMulticast() determines whether the message is forwarded to
   *   the entire subtree under the targeted element, as opposed to just 
   *   the targeted element. 
   * </p>
   * 
   * <p>The default behavior is to propagate the message to the appropriate
   *  children. Subclasses which override this method should call 
   *  this superclass method if an event should be propagated.
   * </p>
   * 
   */
  protected void messageStandard
    (Dispatcher dispatcher
    ,Message message
    )
  {    
    if ((alwaysRunHandlers || dispatcher.isTarget()) && handlerChain!=null)
    { handlerChain.handleMessage(dispatcher,message);
    }
    else
    { relayMessage(dispatcher,message);
    }
  }
  
  @Override
  public final void message(Dispatcher dispatcher,Message message)
  { 
    if (bindError==null)
    { messageStandard(dispatcher,message); 
    }
    
    
  }

  
  
  /**
   * <p>Relay a message to appropriate child elements as indicated by the
   *   path. 
   * </p>
   * 
   * @param context The EventContext associated with this rendering sequence
   * @param message The message to relay
   * @param path The narrowcast path to the element that will receive the 
   *   message
   */
  protected final void relayMessage
    (Dispatcher context
    ,Message message
    )
  { 
    if (innerContext!=null)
    { innerContext.push();
    }
    try
    {
      Integer pathIndex=context.getNextRoute();
      if (pathIndex!=null)
      { messageChild(pathIndex,context,message);
      }
      else if (message.isMulticast())
      { 
        if (children!=null)
        { 
          for (int i=0;i<children.length;i++)
          { messageChild(i,context,message);
          }
        }
      }
      else if (childSubscriptions!=null)
      {
        
        List<Integer> subscribers=childSubscriptions.get(message.getType());
        if (subscribers!=null)
        { 
          for (int i:subscribers)
          { messageChild(i,context,message);
          }
        }
      }      
    }
    catch (RuntimeException x)
    { throw new ElementRuntimeException(this,x);
    }
    finally
    { 
      if (innerContext!=null)
      { innerContext.pop();
      }
    }
    
  }
  
  /**
   * Call this method to message a child element. This just calls 
   *   Dispatcher.relayMessage() to the specified child
   * 
   * @param context
   * @param index
   */
  protected final void messageChild
    (int index
    ,Dispatcher context
    ,Message message
    )
  { 
    if (children==null)
    { 
      log.warning
        ("Element has no children (while looking up route "+index+"): "
         +getDeclarationInfo()
         );
      return;
    }
    
    if (index<children.length)
    { context.relayMessage(children[index],index,message);
    }
    else
    { 
      log.warning
        (getLogPrefix(context)
        +"Message route "+index+" not found: Only "+children.length+" children"
        );
    }
  }
  
  /**
   * <p>Create a new State object for this Element. 
   * </p>
   * 
   * @return An State object for this Element 
   */
  @Override
  public State createState()
  { return new ElementState(children!=null?children.length:0);
  }
  
  /**
   * Find an Element among this Element's ancestors/containers
   * 
   * @param <X>
   * @param clazz
   * @return The Element with the specific class or interface, or null if
   *   none was found
   */
  @SuppressWarnings("unchecked") // Downcast from runtime check
  @Override
  public <X> X findComponent(Class<X> clazz)
  {
    if (clazz.isAssignableFrom(getClass()))
    { return (X) this;
    }
    else if (parent!=null)
    { return parent.<X>findComponent(clazz);
    }
    else
    { return null;
    }
  }
  
  /**
   * Find an Element among this Element's ancestors/containers, but stop
   *   looking when we reach the stop class.
   * 
   * @param <X>
   * @param clazz
   * @return The Element with the specific class or interface, or null if
   *   none was found
   */
  @SuppressWarnings("unchecked") // Downcast from runtime check
  @Override
  public <X> X findComponent(Class<X> clazz,Class<?> stop)
  {
    if (stop.isAssignableFrom(getClass()))
    { return null;
    }
    else if (clazz.isAssignableFrom(getClass()))
    { return (X) this;
    }
    else if (parent!=null)
    { return parent.<X>findComponent(clazz,stop);
    }
    else
    { return null;
    }
    
  }

  /**
   * <p>Find the distance from the calling element's  state in the state
   *   tree to the state of the ancestral element of the specified class.
   * </p>
   * 
   * <p>This method is intended to provide an extremely efficient means
   *   for states to resolve ancestors
   * </p>
   * 
   * @param clazz The class of the Element being searched for.
   * 
   * @return The state distance, where 1 indicates an immediate parent and 0
   *   indicates that this Element matches the requested Class.
   */
  @Override
  public int getStateDistance(Class<?> clazz)
  {
    if (clazz.isAssignableFrom(getClass()))
    { return 0;
    }
    else if (parent!=null)
    { 
      int parentDist=parent.getStateDistance(clazz);
      if (parentDist>-1)
      { return parentDist+getStateDepth();
      }
      else
      { return -1;
      }
    }
    else
    { return -1;
    }
  }
  
  @Override
  public int getStateDepth()
  { return 1;
  }
  
  public int getChildCount()
  { 
    if (children!=null)
    { return children.length;
    }
    else
    { return 0;
    }
  }
  
  @Override
  public void start()
    throws LifecycleException
  { Lifecycler.start(children);
  }
  
  @Override
  public void stop()
    throws LifecycleException
  { Lifecycler.stop(children);
  }
  
  @Override
  public void handleEvent(Dispatcher dispatcher,Event event)
  { dispatcher.handleEvent(event);
  }
  
  @Override
  public Parent asParent()
  { return this;
  }
  
  @Override
  public Component asComponent()
  { return this;
  }
  
  @Override
  public void setDeclarationInfo(DeclarationInfo info)
  { this.declarationInfo=info;
  }
  
  @Override
  public DeclarationInfo getDeclarationInfo()
  { return declarationInfo;
  }
  
  protected Focus<?> getSelfFocus()
  { return selfFocus;
  }
  
  private void registerChild(String id,int index)
  {
    if (idMap==null)
    { idMap=new HashMap<String,Integer>();
    }
    idMap.put(id,index);
  }
  
  protected Integer getChildIndex(String id)
  {
    if (idMap==null)
    { return null;
    }
    else return idMap.get(id);
  }
}
