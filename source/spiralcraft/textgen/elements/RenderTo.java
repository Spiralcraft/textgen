package spiralcraft.textgen.elements;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.kit.AbstractMessageHandler;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Binding;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.OutputContext;
import spiralcraft.textgen.PrepareMessage;
import spiralcraft.textgen.RenderMessage;

/**
 * <p>Render the contents to a different output context immediately after the
 *   Prepare phase. Useful for logging or inserting contextual references into
 *   headers or footers.
 * </p>
 * 
 * @author mike
 *
 */
public class RenderTo<T>
  extends Element
{ 
  private Binding<Appendable> target;
  
  class PrepareHandler
      extends AbstractMessageHandler
  {
    { this.type=PrepareMessage.TYPE;
    }

    @Override
    protected void doHandler(Dispatcher dispatcher, Message message,
            MessageHandlerChain next) 
    { 
      next.handleMessage(dispatcher, message);
      
      if (target!=null)
      {
        Appendable out=target.get();
        if (out!=null)
        {
          OutputContext.push(target.get());
          try
          { relayMessage(dispatcher,RenderMessage.INSTANCE);
          }
          finally
          { OutputContext.pop();
          }
        }
        else
        { log.warning(getDeclarationInfo()+": Output target is null");
        }
      }
      else
      { log.warning(getDeclarationInfo()+": No output target defined");
      }
      
    }
  }
  
  class RenderHandler
    extends AbstractMessageHandler
  { 
    { this.type=RenderMessage.TYPE;
    }
    
    @Override
    protected void doHandler(Dispatcher dispatcher, Message message,
            MessageHandlerChain next) 
    { // Don't render in-context
    }
  }
  
  /**
   * The Appendable to write the output to
   * 
   * @param x
   */
  public void setTarget(Binding<Appendable> target)
  { 
    this.removeParentContextual(this.target);
    this.target=target;
    this.addParentContextual(this.target);
  }
  
  @Override
  protected void addHandlers() 
    throws ContextualException
  {
    super.addHandlers();
    addHandler(new PrepareHandler());
    addHandler(new RenderHandler());
  }
  
}
