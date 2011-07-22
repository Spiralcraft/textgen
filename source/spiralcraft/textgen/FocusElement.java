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


import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.common.ContextualException;
import spiralcraft.common.namespace.ContextualName;
import spiralcraft.common.namespace.PrefixedName;
import spiralcraft.common.namespace.UnresolvedPrefixException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.text.Renderer;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.kit.RenderHandler;

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
@SuppressWarnings("unused")
public abstract class FocusElement<T>
  extends Element
{
  private ThreadLocalChannel<T> channel;
  private Renderer renderer;
  private boolean computeOnInitialize;
  private URI alias;
  
  private LinkedList<Contextual> parentContextuals;
  private LinkedList<Contextual> exportContextuals;
  private LinkedList<Contextual> selfContextuals;    
  
  /**
   * An unique URI under which this value will be published into the
   *   focus chain. 
   * 
   * @param alias
   * @throws UnresolvedPrefixException 
   */
  public void setAlias(ContextualName alias) 
    throws UnresolvedPrefixException
  { this.alias=alias.getQName().toURIPath();
  }  
  
  @Override
  public final Focus<?> bind(Focus<?> focus)
    throws ContextualException
  { 

    if (renderer!=null)
    { addHandler(new RenderHandler(renderer));
    }
    bindParentContextuals(focus);
    bindSelfFocus(focus);
    Focus<?> parentFocus=bindImports(focus);
    
    Channel<T> target=bindSource(parentFocus);
    channel=new ThreadLocalChannel<T>(target.getReflector(),true,target);
    
    bindHandlers(parentFocus);
    
    focus=parentFocus.chain(channel);
    focus.addFacet(getAssembly().getFocus());
    if (alias!=null)
    { focus.addAlias(alias);
    }
    
    focus=bindExports(focus);
    if (focus==null)
    { throw new BindException(getErrorContext()+": Focus cannot be null");
    }
    bindExportContextuals(focus);
    
    bindChildren(focus);
    return focus;
  }
  
  /**
   * This Focus element will recompute its value on initialization. By
   *   convention, data is not normally available on initialization.
   * 
   * @param computeOnInitialize
   */
  public void setComputeOnInitialize(boolean computeOnInitialize)
  { this.computeOnInitialize=computeOnInitialize;
  }
  
  /**
   * <p>Override to set up the context this FocusElement
   * </p>
   * 
   * @param parentFocus
   * @return The new contextual Focus against which the source and exports
   *   will be bound
   * @throws BindException
   */
  protected Focus<?> bindImports(Focus<?> focusChain)
    throws BindException
  { return focusChain;
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
   * @return The value that will be pushed into the context and exported
   *    to children. If the state is non-null, the return value will
   *    be put into the ValueState.
   */
  protected abstract T computeExportValue(ValueState<T> state);
  

  protected final void setRenderer(Renderer renderer)
  { this.renderer=renderer;
  }
  

  
  @Override
  public final void message
    (Dispatcher context
    ,Message message
    )
  {
    push(context,message);
    try
    { super.message(context,message);
    }
    finally
    { pop();
    }
  }

  
  @SuppressWarnings({ "unchecked" })
  private final void invalidateState(Dispatcher context)
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
  private final void push(Dispatcher context,Message message)
  { 
    boolean frameChanged;
    if (!context.isStateful())
    { 
      frameChanged=true;
      channel.push(computeExportValue(null));
    }
    else if (message!=null && message.getType()==InitializeMessage.TYPE)
    { 
      // Don't start the frame on Initialize
      if (computeOnInitialize)
      { 
        ValueState<T> state=(ValueState<T>) context.getState();        
        state.setValue(computeExportValue(state));
        channel.push(state.getValue());
      }
      else
      { channel.push(null);
      }
      frameChanged=false;
    }
    else
    {
      ValueState<T> state=(ValueState<T>) context.getState();
      frameChanged=state.isNewFrame();
      if (frameChanged || !state.isValid())
      { state.setValue(computeExportValue(state));
      }
      channel.push(state.getValue());
    }
    onRecompute(context);
    if (frameChanged)
    { onFrameChange(context);
    }
  }
  
  /**
   * Called after the focus value is recomputed and published into the
   *   context.
   */
  protected void onRecompute(Dispatcher context)
  {
  }
  
  /**
   * Called when the state frame changed
   */
  protected void onFrameChange(Dispatcher context)
  {
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

