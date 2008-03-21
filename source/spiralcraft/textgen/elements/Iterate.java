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
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.IterationCursor;

import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLogger;

import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.IterationState;
import spiralcraft.textgen.MementoState;
import spiralcraft.textgen.Message;
import spiralcraft.textgen.InitializeMessage;
import spiralcraft.textgen.PrepareMessage;

import spiralcraft.textgen.compiler.TglUnit;

import spiralcraft.text.markup.MarkupException;

import java.io.IOException;

import java.util.List;
import java.util.LinkedList;

/**
 * Iterate through an Iterable or an Array
 */
@SuppressWarnings("unchecked") // Runtime type resolution
public class Iterate
  extends Element
{
  private static final ClassLogger log=ClassLogger.getInstance(Iterate.class);
  
  private Expression<?> expression;
  private Focus<?> focus;
  private IterationDecorator decorator;
  private ThreadLocalChannel iterationCursorBinding;
  private boolean initializeContent;

  
  public void setX(Expression<?> expression)
  { this.expression=expression;
  }
  
  public Focus<?> getFocus()
  { return focus;
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
  @SuppressWarnings("unchecked") // Not using generic versions
  public void bind(List<TglUnit> childUnits)
    throws BindException,MarkupException
  { 
    Focus<?> parentFocus=getParent().getFocus();
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
    
    iterationCursorBinding
      =new ThreadLocalChannel(decorator.getComponentReflector());
    
    SimpleFocus simpleFocus=new SimpleFocus
      (iterationCursorBinding);
    
    simpleFocus.setParentFocus(parentFocus);
    focus=simpleFocus;
    bindChildren(childUnits);
  }
  
  
  private void refreshState(IterationState state)
  {
    if (debug)
    { log.fine(toString()+": iterating");
    }
    IterationCursor cursor = decorator.iterator();

    int i=0;
    while (cursor.hasNext())
    { 
      cursor.next();
      state.ensureChild(i++,cursor.getValue());
    }
    state.setStale(false);
    state.trim(i);
    if (debug)
    { log.fine(toString()+": iterated "+i+" elements");
    }
  }
      
  public void message
    (final EventContext genContext
    ,Message message
    ,LinkedList<Integer> path
    )
  {
    IterationState state=(IterationState) genContext.getState();
    if (genContext.isStateful())
    {
      if (message.getType()==InitializeMessage.TYPE)
      {
        if (initializeContent)
        {
          // Run the default iteration for init purposes
          if (debug)
          { log.fine(toString()+": iterating on Initialize");
          }
        
          IterationCursor cursor = decorator.iterator();

          // iterationContextBinding.push(context);

          int i=0;
          while (cursor.hasNext())
          { 
            cursor.next();
            Object val=cursor.getValue();
            try
            {
              iterationCursorBinding.push(val);
              genContext.setState(state.ensureChild(i++,val));
              super.message(genContext,message,path);
            }
            finally
            { iterationCursorBinding.pop();
            }
          }
          state.setStale(false);
          state.trim(i);
          if (debug)
          { log.fine(toString()+": iterated "+i+" elements");
          }
        }
      }
      else
      {
        if (state.isStale())
        {
          if (debug)
          { log.fine(toString()+" refreshing on message because stale");
          }
          refreshState(state);
        }
        else if (message.getType()==PrepareMessage.TYPE)
        { 
          if (debug)
          { log.fine(toString()+" refreshing on Prepare");
          }
          refreshState(state);
        }
      
        if (path==null || path.isEmpty())
        {
          if (debug)
          { log.fine(toString()+": retraversing on message "+state.getChildCount());
          }
          
          // Follow pre-rendered iteration
          for (MementoState childState:state)
          { 
            try
            {
              iterationCursorBinding.push(childState.getValue());
              genContext.setState(childState);
              super.message(genContext,message,path);
            }
            finally
            { iterationCursorBinding.pop();
            }
          }
          
        }
        else
        {
          if (debug)
          { log.fine(toString()+": following path "+state.getChildCount());
          }
          // Follow path
          MementoState childState=state.getChild(path.removeFirst());
          if (childState!=null)
          {
            try
            {
              iterationCursorBinding.push(childState.getValue());
              genContext.setState(childState);
              super.message(genContext,message,path);
            }
            finally
            { iterationCursorBinding.pop();
            }
          }
          
        }
      }
      genContext.setState(state);
      
    }
    
  }
  
  /**
   * Indicate whether the iteration should be re-run from the source on
   *   rendering. Returns true by default.
   * 
   * @param context
   * @return
   */
  protected boolean shouldRegenerate(EventContext context)
  { return true;
  }
  
  public void render(final EventContext genContext)
    throws IOException
  { 
    IterationState state=(IterationState) genContext.getState();
    
    if (shouldRegenerate(genContext))
    {
      if (debug)
      { log.fine(toString()+": iterating on render");
      }
      IterationCursor cursor = decorator.iterator();

      int i=0;
      while (cursor.hasNext())
      { 
        cursor.next();
        try
        {
          iterationCursorBinding.push(cursor.getValue());
          if (genContext.isStateful())
          { genContext.setState(state.ensureChild(i++,cursor.getValue()));
          }
          renderChildren(genContext);
        }
        finally
        { iterationCursorBinding.pop();
        }
      }
      if (state!=null)
      {
        state.setStale(false);
        state.trim(i);
      }
      if (debug)
      { log.fine(toString()+": iterated "+i+" elements");
      }

    }
    else
    {
      if (debug)
      { log.fine(toString()+": retraversing on render "+state.getChildCount());
      }
      // Rerun the previous iteration
      for (MementoState childState:state)
      { 
        try
        {
          iterationCursorBinding.push(childState.getValue());
          genContext.setState(childState);
          renderChildren(genContext);
        }
        finally
        { iterationCursorBinding.pop();
        }
      }
    }
    genContext.setState(state);
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
   * @return
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
}

