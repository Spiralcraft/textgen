package spiralcraft.textgen.compiler;

import java.io.IOException;

//import spiralcraft.log.ClassLogger;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;

import spiralcraft.app.Message;
import spiralcraft.app.State;

/**
 * <p>The bound Element of the IncludeUnit which includes another resource.
 * </p>
 * 
 * <p>The first child is always the included resource. Children after the 
 *   first represent content to be inserted into the included resource.
 * </p>
 * 
 * @author mike
 *
 */
public class IncludeElement
  extends Element
{

//  private static final ClassLogger log
//    =ClassLogger.getInstance(IncludeElement.class);
  
  protected ThreadLocal<State> threadLocalState
    =new ThreadLocal<State>();

  
  @Override
  public void message
    (EventContext context
    ,Message message
    )
  {
    threadLocalState.set(context.getState());
    try
    { 
      Integer pathIndex=context.getNextRoute();
      if (pathIndex!=null)
      { messageChild(pathIndex,context,message);
      }
      else if (message.isMulticast())
      { messageChild(0,context,message);
      }
    }
    finally
    { 
      context.setState(threadLocalState.get());
      threadLocalState.remove();
    }
  }

  /**
   * Message the content (children)  of the IncludeElement from within a 
   *   messaging of the included Element (via the InsertElement)
   * @throws IOException
   */
  public void messageClosure
    (EventContext context
    ,Message message
    )
  {

    State deepState=null;
    try
    { 
      // Save the current state for later restoration and substitute
      //   the state of the actual parent (this IncludeElement's state)
      deepState=context.getState();
      
      context.setState(threadLocalState.get());
      int childCount=getChildCount();
      Integer pathIndex=context.getNextRoute();
      if (pathIndex!=null)
      { this.messageChild(pathIndex,context,message);
      }
      else if (message.isMulticast())
      { 
        for (int i=1;i<childCount;i++)
        { this.messageChild(i,context,message);
        }
      }
    }
    finally
    { context.setState(deepState);
    }
  }


}