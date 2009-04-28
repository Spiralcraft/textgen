//
//Copyright (c) 2009,2009 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.textgen;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.Message;
import spiralcraft.textgen.compiler.TglUnit;

/**
 * <p>Creates a new Focus in the Focus chain that provides the results of
 *   a concrete operation for the duration of an operational cycle as
 *   defined by the a container and the state management mechanism.
 * </p>
 * 
 * <p>To handle messages, install MessageHandlers using addHandler()
 * </p>
 * 
 * <p>To implement any rendering behavior, install a Renderer using
 *   setRenderer()
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public abstract class FocusElement<T>
  extends Element
{
  private ThreadLocalChannel<T> channel;
  private Focus<?> focus;
  private Renderer renderer;
  
  @Override
  public final void bind(List<TglUnit> childUnits)
    throws BindException,MarkupException
  { 

    Focus<?> parentFocus=getParent().getFocus();
    
    Channel<T> target=bindSource(parentFocus);
    channel=new ThreadLocalChannel<T>(target.getReflector());
    
    focus=parentFocus
      .chain(getAssembly().getFocus().getSubject())
      .chain(channel);
    
    focus=bindExports(focus);
    if (focus==null)
    { throw new BindException(getErrorContext()+": Focus cannot be null");
    }
    
    super.bind(childUnits);
  }
  
  /**
   * <p>Override to set up the data source for this FocusElement
   * </p>
   * 
   * @param parentFocus
   * @return The data source that will be cached in the local state
   * @throws BindException
   */
  protected abstract Channel<T> bindSource(Focus<?> focusChain)
    throws BindException;
  
  /**
   * <p>Override to bind anything dependent on the stateful value and 
   *   optionally extend the Focus chain.
   * </p>
   * 
   * @param focusChain A focusChain ending in the Focus that references the
   *   stateful value returned by compute(), The focusChain additionally
   *   contains a Focus that references this Element.
   * @returns Either a new Focus or the provided focusChain (null is prohibited)
   * @throws BindException
   */
  protected abstract Focus<?> bindExports(Focus<?> focusChain)
    throws BindException;
  
  /**
   * <p>Recompute the current value that will be exported by this FocusElement
   * </p>
   * 
   * @return
   */
  protected abstract T compute();
  
  protected final void setRenderer(Renderer renderer)
  { this.renderer=renderer;
  }
  
  @Override
  public final Focus<?> getFocus()
  { return focus;
  }


  @Override
  public final void message
    (EventContext context
    ,Message message
    ,LinkedList<Integer> path
    )
  {
    push(context);
    try
    { super.message(context,message,path);
    }
    finally
    { pop();
    }
  }

  @Override
  public final void render(EventContext context)
    throws IOException
  { 
    push(context);
    
    try
    { 
      if (renderer!=null)
      { renderer.render(context,false);
      }
      renderChildren(context);
      if (renderer!=null)
      { renderer.render(context,true);
      }
    }
    finally
    { pop();
    }
  }
  
  @SuppressWarnings({ "unchecked", "unused" })
  private final void invalidateState(EventContext context)
  { 
    ValueState<T> state=(ValueState<T>) context.getState();
    if (state!=null)
    { state.invalidate();
    }
  }
  
  /**
   * Call this to put the value into the thread context
   */
  @SuppressWarnings("unchecked")
  private final void push(EventContext context)
  { 
    if (!context.isStateful())
    { channel.push(compute());
    }
    else
    {
      ValueState<T> state=(ValueState<T>) context.getState();
      if (state.frameChanged(context.getCurrentFrame()) || !state.isValid())
      { state.setValue(compute());
      }
      channel.push(state.getValue());
    }
  }
  
  /**
   * Call this from within a finally block to pop the value from the
   *   thread context
   */
  private final void pop()
  { channel.pop();
  }
  
  @Override
  public ValueState<T> createState()
  { return new ValueState<T>(this);
  }    
  
  
}

