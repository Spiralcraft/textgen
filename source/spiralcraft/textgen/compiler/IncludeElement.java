package spiralcraft.textgen.compiler;

import java.io.IOException;

//import spiralcraft.log.ClassLogger;
import spiralcraft.textgen.Element;

import spiralcraft.app.Dispatcher;
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
  protected void messageStandard
    (Dispatcher context
    ,Message message
    )
  {
    State state=context.getState();
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
      threadLocalState.remove();
      if (context.getState()!=state)
      { 
        throw new IllegalStateException
          ("Found "+context.getState()+" expected "+state);
      }
    }
  }

  /**
   * Message the content (children)  of the IncludeElement from within a 
   *   messaging of the included Element (via the InsertElement)
   * @throws IOException
   */
  public void messageClosure
    (Dispatcher context
    ,Message message
    )
  {

    State deepState=context.getState();
    try
    { 
      
      int childCount=getChildCount();
      Integer pathIndex=context.getNextRoute();
      if (pathIndex!=null)
      { 
        if (pathIndex<getChildCount())
        { 
          context.relayMessage
            (getChild(pathIndex)
            ,threadLocalState.get()
            ,pathIndex
            ,message
            );
        }
        else
        {
          log.warning
          (getLogPrefix(context)
          +"Route error: "+pathIndex+">="+getChildCount()
          );
        }
      }
      else if (message.isMulticast())
      { 
        for (int i=1;i<childCount;i++)
        { 
          context.relayMessage
            (getChild(i)
            ,threadLocalState.get()
            ,i
            ,message
            );
        }
      }
    }
    finally
    { 
      if (context.getState()!=deepState)
      { 
        throw new IllegalStateException
          ("Found "+context.getState()+" expected "+deepState);
      }
    }
  }


}