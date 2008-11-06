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

import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

import spiralcraft.builder.Assembly;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

import spiralcraft.textgen.compiler.TglUnit;

import spiralcraft.text.ParsePosition;
import spiralcraft.text.markup.MarkupException;

import java.net.URI;


/**
 * <P>A compositional unit of a TGL document.
 * 
 * <P>An Element may contain other Elements and/or content, forming a tree of
 *   Elements which generate output.
 *   
 * <P>Elements are thread-safe, and thus must not maintain dynamic 
 *   state internally. An element resolves its state within the render()
 *   or message() methods, and uses the EventContext object to provide
 *   a reference its state.
 *   
 * <P>The Elements associate with application specific runtime context via the
 *   Focus, which is provided to the root Element by the container. Elements may
 *   extend this Focus to refine the context for their child elements. The
 *   spiralcraft.lang expression language is used to access application context
 *   through this Focus.
 *   
 * <P>Elements are beans which are instantiated and configured 
 *   (ie. parameterized) using the spiralcraft.builder package. Each Element
 *   is associated with an Assembly. The Assembly provides a means for 
 *   Elements to associate with other elements in the Element structure
 *   without those elements being adjacent to each other.
 * 
 * <P>Elements are created by first instantiating them and applying the bean
 *   properties specified in their TGL declarations. The Element is then bound
 *   to its already bound parent Element, where it is able to resolve any
 *   expressions by binding them to the Focus provided by its parent element. 
 */
public abstract class Element
{ 

  private Element[] children;
  private Element parent;
  private Assembly<?> assembly;
  private String id;
  private ArrayList<MessageHandler> handlers;
  protected boolean debug;
  private ParsePosition position;
  
  public void setCodePosition(ParsePosition position)
  { this.position=position.clone();
  }
  
  protected String getErrorContext()
  { return " "+position.toString()+(id!=null?" id=["+id+"]: ":" ");
  }
  
  // private int[] path;

  /**
   * Specify an id for this Element and make it visible to the expression
   *   language when used anywhere inside this element. 
   */
  public void setId(String id)
  { this.id=id;
  }
  
  public String getId()
  { return id;
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  public boolean isDebug()
  { return debug;
  }
  
  protected synchronized void addHandler(MessageHandler handler)
  { 
    if (handlers==null)
    { handlers=new ArrayList<MessageHandler>();
    }
    handlers.add(handler);
  }
  
  /*
  public int[] getPath()
  { return path;
  }
  
  public void setPath(int[] path)
  { 
    this.path=path;
    System.err.println("Element.setPath(): "+ArrayUtil.format(path,"/",null));
    if (children!=null)
    {
      for (int i=0;i<children.length;i++)
      {
        int[] childPath=new int[path.length+1];
        System.arraycopy(path,0,childPath,0,path.length);
        childPath[childPath.length-1]=i;
        children[i].setPath(childPath);
      }
    }
  }
  */
  
  protected Element getChild(int i)
  { 
    if (children==null)
    { return null;
    }
    return children[i];
  }
  
  /**
   * @return The Focus associated with this Element. Defaults to the parent
   *   Element's Focus, unless overridden.
   */
  public Focus<?> getFocus()
  { 
    if (parent!=null)
    { return parent.getFocus();
    }
    System.err.println("Element: "+getClass().getName()+" no parent, no focus");
    return null;
  }
  
  /**
   * @return The context URI associated with this Element, which is generally
   *   the URI of the directory that contains the TGL source file. 
   */
  public URI getContextURI()
  {
    if (parent!=null)
    { return parent.getContextURI();
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
   */
  public void setParent(Element parent)
    throws MarkupException
  { 
    if (this.parent!=null)
    { throw new IllegalStateException("Parent already specified");
    }
    this.parent=parent;
  }
  
  public Element getParent()
  { return parent;
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
   */
  public void bind(List<TglUnit> childUnits)
    throws BindException,MarkupException
  { bindChildren(childUnits);
  }

  protected void bindChildren
    (List<TglUnit> childUnits
    )
    throws MarkupException
  {
    if (childUnits!=null)
    { 
      children=new Element[childUnits.size()];
      int i=0;
      for (TglUnit child: childUnits)
      { children[i++]=child.bind(this);
      }
    }
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
  public void message
    (EventContext context
    ,Message message
    ,LinkedList<Integer> path
    )
  {    
    if (handlers!=null)
    {
      for (MessageHandler handler: handlers)
      { handler.handleMessage(context,message,false);
      }
    }
    
    relayMessage(context,message,path);

    if (handlers!=null)
    {
      for (MessageHandler handler: handlers)
      { handler.handleMessage(context,message,true);
      }
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
    (EventContext context
    ,Message message
    ,LinkedList<Integer> path
    )
  { 
    if (path!=null && !path.isEmpty())
    { messageChild(path.removeFirst(),context,message,path);
    }
    else if (message.isMulticast())
    { 
      if (children!=null)
      { 
        for (int i=0;i<children.length;i++)
        { messageChild(i,context,message,path);
        }
      }
    }
  }
  
  /**
   * Call this method to message a child element.
   * 
   * <P>This method ensures that the child Element's state is available in the
   *   eventContext, and ensures that this Element's state is restored to
   *   the eventContext upon return
   * </P>
   * 
   * @param context
   * @param index
   */
  protected final void messageChild
    (int index
    ,EventContext context
    ,Message message
    ,LinkedList<Integer> path
    )
  { 
    if (context.isStateful())
    {
      ElementState state=context.getState();
      try
      {
        context.setState(ensureChildState(state,index));
        children[index].message(context,message,path);
      }
      finally
      { context.setState(state);
      }
    }
    else
    { children[index].message(context,message,path);
    }
  }
  
  private ElementState ensureChildState(ElementState parentState,int index)
  {
    ElementState childState=parentState.getChild(index);
    if (childState==null)
    { 
      childState=children[index].createState();
      parentState.setChild(index,childState);
    }
    return childState;
  }
  
  /**
   * <p>Recursively perform processing and write output. The implementation
   *   of this method should call renderChild() for each child tree that
   *   should be rendered.
   * </p>
   * 
   */
  public abstract void render(EventContext context)
    throws IOException;
  
  
  /**
   * <P>Call this method to render a child element.
   * </P>
   * 
   * <P>This method ensures that the child Element's state is available in the
   *   eventContext, and ensures that this Element's state is restored to
   *   the eventContext upon return
   * </P>
   * 
   * @param context
   * @param index
   * @throws IOException
   */
  protected final void renderChild(EventContext context,int index)
    throws IOException
  { 
    if (context.isStateful())
    {
      ElementState state=context.getState();
      try
      {
        context.setState(ensureChildState(state,index));
        children[index].render(context);
      }
      finally
      { context.setState(state);
      }
    }
    else
    { children[index].render(context);
    }
  }
  
  /**
   * Convenience method to render all of this Element's children
   * 
   * @param context
   * @throws IOException
   */
  protected void renderChildren(EventContext context)
    throws IOException
  {
    if (children!=null)
    { 
      for (int i=0;i<children.length;i++)
      { renderChild(context,i);
      }
    }
  }

  /**
   * <p>Create a new ElementState object for this Element. 
   * </p>
   * 
   * @return An ElementState object for this Element 
   */
  public ElementState createState()
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
  public <X> X findElement(Class<X> clazz)
  {
    if (clazz.isAssignableFrom(getClass()))
    { return (X) this;
    }
    else if (parent!=null)
    { return parent.<X>findElement(clazz);
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
  public <X> X findElement(Class<X> clazz,Class<?> stop)
  {
    if (stop.isAssignableFrom(getClass()))
    { return null;
    }
    else if (clazz.isAssignableFrom(getClass()))
    { return (X) this;
    }
    else if (parent!=null)
    { return parent.<X>findElement(clazz,stop);
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
  public int getStateDistance(Class<?> clazz)
  {
    if (clazz.isAssignableFrom(getClass()))
    { return 0;
    }
    else if (parent!=null)
    { 
      int parentDist=parent.getStateDistance(clazz);
      if (parentDist>-1)
      { return parentDist+1;
      }
      else
      { return -1;
      }
    }
    else
    { return -1;
    }
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
  
}
