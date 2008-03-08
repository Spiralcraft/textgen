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

import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.IterationState;
import spiralcraft.textgen.MementoState;
import spiralcraft.textgen.Message;
import spiralcraft.textgen.InitializeMessage;

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
  
  private Expression<?> expression;
  private Focus<?> focus;
  private IterationDecorator decorator;
  private ThreadLocalChannel iterationCursorBinding;

  
  public void setX(Expression<?> expression)
  { this.expression=expression;
  }
  
  public Focus<?> getFocus()
  { return focus;
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
        // Run the default iteration for init purposes
        
        IterationCursor cursor = decorator.iterator();

        // iterationContextBinding.push(context);

        int i=0;
        while (cursor.hasNext())
        { 
          cursor.next();
          try
          {
            iterationCursorBinding.push(cursor.getValue());
            genContext.setState(state.ensureChild(i++,cursor.getValue()));
            super.message(genContext,message,path);
          }
          finally
          { iterationCursorBinding.pop();
          }
        }
      }
      else
      {
      
        if (path==null || path.isEmpty())
        {
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
      IterationCursor cursor = decorator.iterator();

      // iterationContextBinding.push(context);

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
    }
    else
    {
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

