package spiralcraft.textgen.elements;

import spiralcraft.lang.util.ChannelBuffer;
import spiralcraft.textgen.ValueState;
import spiralcraft.time.Clock;

class WithState<T>
  extends ValueState<T>
{
  private volatile long lastCompute;
  public final ChannelBuffer<Object> trigger;
  private volatile boolean forceCompute;
  
  public WithState(With<T> control,ChannelBuffer<Object> trigger)
  {
    super(control);
    this.trigger=trigger;
  }
  
  public long getLastComputeTime()
  { return lastCompute;
  }
  
  public void forceCompute()
  { forceCompute=true;
  }
  
  public boolean getForceCompute()
  { return forceCompute;
  }
  
  public void computed()
  { 
    lastCompute=Clock.instance().approxTimeMillis();
    forceCompute=false;
  }
  

  
  
}