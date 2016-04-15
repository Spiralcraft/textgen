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

import spiralcraft.app.kit.StateReferenceHandler;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.util.ChannelBuffer;
import spiralcraft.textgen.ValueState;



/**
 * <p>Publishes the result of an Expression into the Focus chain for the
 *   lifetime of the associated State or until a trigger changes.
 * </p>
 * 
 * <p>The expression is not recomputed every time the StateFrame changes
 * </p>
 * 
 * 
 * @author mike
 *
 * @param <T>
 */
public class Session<T>
  extends With<T>
{
  
  protected Binding<Void> onStartX;
  
  @SuppressWarnings({"unchecked","rawtypes"})
  private StateReferenceHandler<SessionState<T>> stateRef
    =new StateReferenceHandler(SessionState.class);
  
  { 
    defaultRecompute=false;
    alwaysRunHandlers=true;
  }
  
  public void setOnStart(Binding<Void> onStart)
  { 
    removeParentContextual(this.onStartX);
    this.onStartX=onStart;
    addParentContextual(this.onStartX);
  }

  @Override
  public SessionState<T> createState()
  { return new SessionState<T>(this,new ChannelBuffer<Object>(triggerKeyX));
  } 

  public void reset()
  { stateRef.get().forceCompute();
  }
  
  /**
   * Called when the Stateful value should be recomputed
   * 
   */
  @Override  
  protected T computeExportValue(ValueState<T> state)
  { 
    if ( !((SessionState<T>) state).seen() && onStartX!=null)
    { onStartX.get();
    }
    return super.computeExportValue(state); 
  }

 
  @Override
  protected void addHandlers()
    throws ContextualException
  {
    super.addHandlers();
    addHandler(stateRef);
  }
  
}

class SessionState<T>
  extends WithState<T>
{
  private boolean seen;
  
  public SessionState(Session<T> control,ChannelBuffer<Object> trigger)
  { super(control,trigger);
  }

  public boolean seen()
  { 
    if (!seen)
    { 
      seen=true;
      return false;
    }
    else
    { return true;
    }
  }
}

