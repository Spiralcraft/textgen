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
package spiralcraft.textgen.elements;

import spiralcraft.lang.BindException;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.Focus;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.IterationDecorator;

import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLog;

import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.IterationState;
import spiralcraft.textgen.MementoState;
import spiralcraft.textgen.Message;

import spiralcraft.textgen.InitializeMessage;


import spiralcraft.text.markup.MarkupException;
import spiralcraft.util.LookaroundIterator;

import java.io.IOException;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Iterate through an Iterable or an Array
 */
@SuppressWarnings({"unchecked","rawtypes"}) // Runtime type resolution
public class Iterate
  extends Element
{
  private static final ClassLog log=ClassLog.getInstance(Iterate.class);
  
  private Expression<?> expression;
  private Focus<?> currentFocus;
  private Focus<?> lookaheadFocus;
  private Focus<?> lookbehindFocus;

  private IterationDecorator decorator;
  private ThreadLocalChannel valueChannel;
  private ThreadLocalChannel lookaheadChannel;
  private ThreadLocalChannel lookbehindChannel;
  
  private ThreadLocal<Iteration> iterationLocal
    =new ThreadLocal<Iteration>();
  
  private boolean initializeContent;

  
  public void setX(Expression<?> expression)
  { this.expression=expression;
  }
  
  /**
   * 
   * @return The current index of the iteration being performed by the 
   *   current Thread
   */
  public int getIndex()
  { return iterationLocal.get().index;
  }
  
  /**
   * 
   * @return Whether the iteration being performed by the 
   *   current Thread is on the last element
   */
  public boolean isLast()
  { return !iterationLocal.get().hasNext;
  }
  
  /**
   * 
   * @return Whether the iteration being performed by the 
   *   current Thread is on the last element
   */
  public boolean isFirst()
  { return iterationLocal.get().index==0;
  }

  public Focus<?> getFocus()
  { return currentFocus;
  }
  
  public Focus<?> getLookaheadFocus()
  { return lookaheadFocus;
  }
  
  public Focus<?> getLookbehindFocus()
  { return lookbehindFocus;
  }

  /**
   * <p>Indicate whether the Initialize message should run the iteration and
   *   propogate the message to any content created as a result.
   * </p>
   * 
   * <p>The default value is false, to avoid the unnecessary retrieval of
   *   potentially expensive data.
   * </p>
   *   
   * @param val
   */
  public void setInitializeContent(boolean val)
  { initializeContent=val;
  }
  
  @Override
  public Focus<?> bind(Focus<?> parentFocus)
    throws BindException,MarkupException
  { 
    Channel<?> target=null;
    if (expression!=null)
    { target=parentFocus.bind(expression);
    }
    else
    { 
      target=parentFocus.getSubject();
      if (target==null)
      { throw new BindException("Focus "+parentFocus+" has no subject");
      }
    }
    
    
    
    decorator=
      target.<IterationDecorator>decorate(IterationDecorator.class);
    
    if (decorator==null)
    { 
      throw new BindException
        ("Cannot iterate through a "+target.getContentType().getName());
    }
    
    // iterationContextBinding
    //  =new ThreadLocalChannel<IterationContext>
    //    (BeanReflector.<IterationContext>getInstance(IterationContext.class)
    //    );
    
    
    // SimpleFocus simpleFocus=new SimpleFocus
    //  (decorator.createComponentBinding(iterationContextBinding));
    
    {
      valueChannel
        =new ThreadLocalChannel(decorator.getComponentReflector());
    

      SimpleFocus compoundFocus
        =new SimpleFocus(parentFocus,valueChannel);
    
      compoundFocus.addFacet
        (this.getAssembly().getFocus());
      currentFocus=compoundFocus;
    }
    
    {
      lookaheadChannel
        =new ThreadLocalChannel(decorator.getComponentReflector());

      SimpleFocus compoundFocus
        =new SimpleFocus(parentFocus,lookaheadChannel);
    
      compoundFocus.addFacet
        (this.getAssembly().getFocus());
      lookaheadFocus=compoundFocus;
      
    }
    
    {
      lookbehindChannel
        =new ThreadLocalChannel(decorator.getComponentReflector());

      SimpleFocus compoundFocus
        =new SimpleFocus(parentFocus,lookbehindChannel);
    
      compoundFocus.addFacet(this.getAssembly().getFocus());
      lookbehindFocus=compoundFocus;
      
    }
    
    if (debug)
    { log.fine("Iterator exposes "+valueChannel);
    }
    
    return super.bind(currentFocus);
  }
  
  /**
   * Called from messageStateful to refresh state before propagating message
   * 
   * @param state
   */
  private void refreshState(IterationState state)
  {
    if (debug)
    { log.fine(toString()+": iterating");
    }
    Iterator<?> cursor = decorator.iterator();

    int i=0;
    while (cursor.hasNext())
    { state.ensureChild(i++,cursor.next());
    }
    state.trim(i);
    if (debug)
    { log.fine(toString()+": iterated "+i+" elements");
    }
  }
      
  
  
  private void messageInitializeContent
    (final EventContext genContext
    ,Message message
    ,LinkedList<Integer> path
    ,IterationState state
    )
  {
    // Run the default iteration for init purposes
    if (debug)
    { log.fine(toString()+": iterating on Initialize");
    }

    LookaroundIterator<?> cursor = new LookaroundIterator(decorator.iterator());

    Iteration oldIter=iterationLocal.get();
    Iteration iter=new Iteration();
    iterationLocal.set(iter);

    try
    {

      int i=0;
      while (cursor.hasNext())
      { 
        Object lastVal=cursor.getPrevious();
        Object val=cursor.next();

        iter.index=i;
        iter.hasNext=cursor.hasNext();

        try
        {
          lookbehindChannel.push(lastVal);
          valueChannel.push(val);
          lookaheadChannel.push(cursor.getCurrent());
          genContext.setState(state.ensureChild(i++,val));
          super.message(genContext,message,path);
        }
        finally
        { 
          lookaheadChannel.pop();
          valueChannel.pop();
          lookbehindChannel.pop();
        }
      }
      
      state.trim(i);
      if (debug)
      { log.fine(toString()+": iterated "+i+" elements");
      }

    }
    finally
    {
      if (oldIter!=null)
      { iterationLocal.set(oldIter);
      }
      else
      { iterationLocal.remove();
      }
    }
          
    
  }
  
  private void messageStatefulChildren
    (final EventContext genContext
    ,Message message
    ,LinkedList<Integer> path
    ,IterationState state
    )
  {
    Iteration oldIter=iterationLocal.get();
    Iteration iter=new Iteration();
    iterationLocal.set(iter);

    try
    {
      if (debug)
      { 
        log.fine(toString()+": retraversing on message "+message+" ("
          +state.getChildCount()+" elements)");
      }

      // Follow pre-rendered iteration
      LookaroundIterator<MementoState> it
        =new LookaroundIterator<MementoState>(state.iterator());
      
      while (it.hasNext())
      {
        Object lastVal=it.getPrevious()!=null?it.getPrevious().getValue():null;
        MementoState childState=it.next();
        iter.hasNext=it.hasNext();
        
        try
        {
          lookbehindChannel.push(lastVal);
          valueChannel.push(childState.getValue());
          lookaheadChannel.push
            (it.getCurrent()!=null?it.getCurrent().getValue():null);
          genContext.setState(childState);
          super.message(genContext,message,path);
        }
        finally
        { 
          lookaheadChannel.pop();
          valueChannel.pop();
          lookbehindChannel.pop();
        }
        
        iter.index++;
      }
          
    }
    finally
    {
      if (oldIter!=null)
      { iterationLocal.set(oldIter);
      }
      else
      { iterationLocal.remove();
      }
    }
    
  }
  
  private void messageStatefulChild
    (final EventContext genContext
    ,Message message
    ,LinkedList<Integer> path
    ,MementoState childState
    ,int index
    )
  {
    Iteration oldIter=iterationLocal.get();
    Iteration iter=new Iteration();
    iterationLocal.set(iter);

    iter.index=index;
    iter.hasNext
      =index<((IterationState) genContext.getState()).getChildCount()-1;

    try
    {
      lookbehindChannel.push(null);
      valueChannel.push(childState.getValue());
      lookaheadChannel.push(null);
      genContext.setState(childState);
      super.message(genContext,message,path);
    }
    finally
    { 
      lookaheadChannel.pop();
      valueChannel.pop();
      lookbehindChannel.pop();
      if (oldIter!=null)
      { iterationLocal.set(oldIter);
      }
      else
      { iterationLocal.remove();
      }
    }
    
  }
  
  private void messageStateful
    (final EventContext genContext
    ,Message message
    ,LinkedList<Integer> path
    )
  {
    IterationState state=(IterationState) genContext.getState();
    try
    {
    
      if (message.getType()==InitializeMessage.TYPE)
      {
        if (initializeContent)
        { messageInitializeContent(genContext,message,path,state);
        }
      }
      else
      {
        if (state.frameChanged(genContext.getCurrentFrame()))
        { 
          if (debug)
          { log.fine(toString()+" refreshing on Frame change");
          }
          refreshState(state);
        }
      
        if (path==null || path.isEmpty())
        { messageStatefulChildren(genContext,message,path,state);
        }
        else
        { 
          if (debug)
          { log.fine(toString()+": following path "+state.getChildCount());
          }
          // Follow path
          int index=path.removeFirst();
          MementoState childState=(MementoState) state.getChild(index);
          if (childState!=null)
          { messageStatefulChild(genContext,message,path,childState,index);
          }
          
        }
      }
    }
    finally
    { genContext.setState(state);
    }
  }
  
  @Override
  public void message
    (final EventContext context
    ,Message message
    ,LinkedList<Integer> path
    )
  {
    if (context.isStateful())
    { messageStateful(context,message,path);
    }    
  }
  
      
  private void renderRegenerate
    (final EventContext genContext
    ,IterationState state
    )
    throws IOException
  {
    Iteration oldIter=iterationLocal.get();
    Iteration iter=new Iteration();
    iterationLocal.set(iter);

    try
    {
      // Efficient case- run the iteration while we render

      if (debug)
      { log.fine(toString()+": iterating on render");
      }
      LookaroundIterator cursor = new LookaroundIterator(decorator.iterator());

      int i=0;
      while (cursor.hasNext())
      { 
        Object lastVal=cursor.getPrevious();
        Object val=cursor.next();

        iter.index=i;
        iter.hasNext=cursor.hasNext();

        try
        {
          lookbehindChannel.push(lastVal);
          valueChannel.push(val);
          lookaheadChannel.push(cursor.getCurrent());
          if (genContext.isStateful())
          { genContext.setState(state.ensureChild(i,val));
          }
          renderChildren(genContext);
        }
        finally
        { 
          lookaheadChannel.pop();
          valueChannel.pop();
          lookbehindChannel.pop();
        }
        i++;
      }
      if (state!=null)
      { state.trim(i);
      }
      if (debug)
      { log.fine(toString()+": iterated "+i+" elements");
      }
        
    }
    finally
    {
      if (oldIter!=null)
      { iterationLocal.set(oldIter);
      }
      else
      { iterationLocal.remove();
      }
    }        
  }

  private void renderRetraverse
    (final EventContext genContext
    ,IterationState state
    )
    throws IOException
  {
    Iteration oldIter=iterationLocal.get();
    Iteration iter=new Iteration();
    iterationLocal.set(iter);

    try
    {
      if (debug)
      { 
        log.fine(toString()+": retraversing on render ("
            +state.getChildCount()+" elements)");
      }
      
      // Follow pre-rendered iteration
      LookaroundIterator<MementoState> it
        =new LookaroundIterator<MementoState>(state.iterator());
      while (it.hasNext())
      {
        Object lastVal=it.getPrevious()!=null?it.getPrevious().getValue():null;
        MementoState childState=it.next();
        iter.hasNext=it.hasNext();
        try
        {
          lookbehindChannel.push(lastVal);
          valueChannel.push(childState.getValue());
          lookaheadChannel.push
            (it.getCurrent()!=null?it.getCurrent().getValue():null);
          genContext.setState(childState);
          renderChildren(genContext);
        }
        finally
        { 
          lookaheadChannel.pop();
          valueChannel.pop();
          lookbehindChannel.pop();
        }
        iter.index++;
      }
    }
    finally
    {
      if (oldIter!=null)
      { iterationLocal.set(oldIter);
      }
      else
      { iterationLocal.remove();
      }
    }
    
  }
  
  @Override
  public void render(final EventContext genContext)
    throws IOException
  { 
    IterationState state=(IterationState) genContext.getState();
    try
    {
    
      if (state==null || state.frameChanged(genContext.getCurrentFrame()))
      { renderRegenerate(genContext,state);
      }
      else
      { renderRetraverse(genContext,state);
      }
    }
    finally
    { genContext.setState(state);
    }
  }
  
  /**
   * <p>Find the distance from the calling element's state in the state
   *   tree to the state of the element of the specified class.
   * </p>
   * 
   * <p>This method overrides the same method in Element to reflect the
   *   fact that an Iteration contributes a depth of 2 states to the 
   *   state tree.
   * </p>
   * 
   * @param clazz
   * @return The state distance for an iteration
   */
  @Override
  public int getStateDistance(Class<?> clazz)
  {
    if (clazz.isAssignableFrom(getClass()))
    { return 1;
    }
    else 
    { 
      int superDist=super.getStateDistance(clazz);
      if (superDist>-1)
      { return superDist+1;
      }
      else
      { return -1;
      }
    }    
  }  
  
  @Override
  public IterationState createState()
  { return new IterationState(getChildCount());
  }
  
  class Iteration
  {
    public int index;
    public boolean hasNext;
    
  }
  
}

