package spiralcraft.textgen.compiler;

import spiralcraft.app.State;
import spiralcraft.app.StateFrame;
import spiralcraft.util.Sequence;

public class TextState
  implements State
{
  private State parent;
  
  @Override
  public Sequence<Integer> getPath()
  { return null;
  }

  @Override
  public State getParent()
  { return parent;
  }

  @Override
  public State getChild(
    int index)
  { return null;
  }

  @Override
  public void link(
    State parentState,
    Sequence<Integer> path)
  { this.parent=parentState;
  }

  @Override
  public void setChild(
    int index,
    State child)
  { throw new IllegalArgumentException("TextState cannot have children");
  }

  @Override
  public <X> X findState(
    Class<X> clazz)
  { return null;
  }

  @Override
  public State getAncestor(
    int distance)
  { return null;
  }

  @Override
  public String getLocalId()
  { return null;
  }

  @Override
  public void enterFrame(
    StateFrame frame)
  { 
  }

  @Override
  public void exitFrame()
  { 
  }

  @Override
  public StateFrame getFrame()
  { return null;
  }

  @Override
  public boolean isNewFrame()
  { return false;
  }

}