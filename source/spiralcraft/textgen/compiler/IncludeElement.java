package spiralcraft.textgen.compiler;

import java.io.IOException;
import java.util.LinkedList;

//import spiralcraft.log.ClassLogger;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.ElementState;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.Message;

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

  @Override
  public void message
    (EventContext context
    ,Message message
    ,LinkedList<Integer> path
    )
  {
    threadLocalState.set(context.getState());
    try
    { 
      if (path!=null && !path.isEmpty())
      { messageChild(path.removeFirst(),context,message,path);
      }
      else if (message.isMulticast())
      { messageChild(0,context,message,path);
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
    ,LinkedList<Integer> path
    )
  {

    ElementState deepState=null;
    try
    { 
      // Save the current state for later restoration and substitute
      //   the state of the actual parent (this IncludeElement's state)
      deepState=context.getState();
      context.setState(threadLocalState.get());
      int childCount=getChildCount();
      if (path!=null && !path.isEmpty())
      { this.messageChild(path.removeFirst(),context,message,path);
      }
      else if (message.isMulticast())
      { 
        for (int i=1;i<childCount;i++)
        { this.messageChild(i,context,message,path);
        }
      }
    }
    finally
    { context.setState(deepState);
    }
  }

  /**
   * Render the content (children)  of the IncludeElement from within a 
   *   rendering of the included Element (via the InsertElement)
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