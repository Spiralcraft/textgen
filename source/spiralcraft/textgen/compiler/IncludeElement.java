package spiralcraft.textgen.compiler;

import java.io.IOException;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.ElementState;
import spiralcraft.textgen.EventContext;

public class IncludeElement
  extends Element
{

  protected ThreadLocal<ElementState> threadLocalState
  =new ThreadLocal<ElementState>();

  @Override
  public void render(EventContext context)
    throws IOException
  { 
    threadLocalState.set(context.getState());
    try
    { renderChild(context,0);
    }
    finally
    { 
      context.setState(threadLocalState.get());
      threadLocalState.remove();
    }
  }

  /**
   * Render the content (children)  of the IncludeElement from within a 
   *   rendering of the included Element.
   * @throws IOException
   */
  public void renderClosure(EventContext context)
    throws IOException
  {

    ElementState deepState=null;
    try
    { 
      // Save the current state for later restoration and substitute
      //   the state of the actual parent (this IncludeElement's state)
      deepState=context.getState();
      context.setState(threadLocalState.get());
      int childCount=getChildCount();
      for (int i=1;i<childCount;i++)
      { 
        // System.err.println("IncludeElement: rendering "+i);
        renderChild(context,i);
      }
    }
    finally
    { context.setState(deepState);
    }
  }

}