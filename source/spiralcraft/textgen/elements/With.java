//
// Copyright (c) 2010 Michael Toth
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

import spiralcraft.lang.Binding;
import spiralcraft.lang.util.ChannelBuffer;
import spiralcraft.textgen.ExpressionFocusElement;
import spiralcraft.textgen.ValueState;
import spiralcraft.time.Clock;


/**
 * <p>Publishes the result of an Expression into the Focus chain.
 * </p>
 * 
 * <p>The expression is recomputed every time the StateFrame changes, unless
 *   otherwise configured.
 * </p>
 * 
 * 
 * @author mike
 *
 * @param <T>
 */
public class With<T>
  extends ExpressionFocusElement<T>
{

  protected Binding<Boolean> holdWhileX;
  protected Binding<Object> triggerKeyX;
  protected boolean defaultRecompute=true;
  
  /**
   * <p>An Expression that will cause the exported value to be held (not
   *   recomputed) in the associated Element State as long as it returns "true"
   *   and no other situation causes the value to be recomputed.  The value 
   *   will always be computed once, initially. 
   * </p> 
   * 
   * <p>For example, if the expression is the boolean literal 'true', the
   *   value will be computed once and held indefinitely for the given
   *   Element State
   * </p> 
   * 
   * @param holdWhileX
   */
  public void setHoldWhileX(Binding<Boolean> holdWhileX)
  { 
    removeParentContextual(this.holdWhileX);
    this.holdWhileX=holdWhileX;
    addParentContextual(this.holdWhileX);
  }
  
  /**
   * <p>An Expression that will cause the exported value to be held (not
   *   recomputed) in the associated Element State as long as it returns the 
   *   same value, and no other situation causes the value to be recomputed. 
   *   The value will always be computed once, initially.
   * </p>
   * 
   * <p>For example, if the expression returns the hour of the day, the
   *   value will be recomputed once initially, and then again each time
   *   the hour changes.
   * </p>
   *   
   * @param triggerKeyX
   */
  public void setTriggerKeyX(Binding<Object> triggerKeyX)
  { 
    removeParentContextual(this.triggerKeyX);
    this.triggerKeyX=triggerKeyX;
    addParentContextual(this.triggerKeyX);
  } 
  
  /**
   * Called when the Stateful value should be recomputed
   * 
   */
  @Override  
  protected T computeExportValue(ValueState<T> state)
  { 
    WithState<T> withState=(WithState<T>) state;
    if (state==null)
    { return super.computeExportValue(state);
    }
    
    
    boolean recompute=defaultRecompute;
    
    if (defaultRecompute)
    {
      // Recompute by default for each frame
      
      if (holdWhileX!=null 
          && withState.getLastComputeTime()>0
          && Boolean.TRUE.equals(holdWhileX.get())
          )
      { recompute=false;
      }
    
      if (triggerKeyX!=null)
      {
        if (!withState.trigger.update() 
              && withState.getLastComputeTime()>0
           )
        { recompute=false;
        }
      }
    }
    else
    {
      // Don't recompute by default (Session mode)
      
      if (withState.getLastComputeTime()<=0)
      { recompute=true;
      }
      
      if (!recompute
          && holdWhileX!=null 
          && !Boolean.TRUE.equals(holdWhileX.get())
          )
      { recompute=true;
      }
         
      if (triggerKeyX!=null && withState.trigger.update())
      { recompute=true;
      }
      
    }
    
    if (recompute)
    { 
      T ret=super.computeExportValue(state); 
      withState.computed();
      return ret;      
    }
    else
    { return state.getValue();
    }
  }
  
  @Override
  public WithState<T> createState()
  { return new WithState<T>(this,new ChannelBuffer<Object>(triggerKeyX));
  }   
}

class WithState<T>
  extends ValueState<T>
{
  private volatile long lastCompute;
  public final ChannelBuffer<Object> trigger;
  
  public WithState(With<T> control,ChannelBuffer<Object> trigger)
  {
    super(control);
    this.trigger=trigger;
  }
  
  public long getLastComputeTime()
  { return lastCompute;
  }
  
  public void computed()
  { lastCompute=Clock.instance().approxTimeMillis();
  }
  

  
  
}
