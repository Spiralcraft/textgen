package spiralcraft.textgen.elements;

import java.io.IOException;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.kit.AbstractMessageHandler;
import spiralcraft.app.kit.StateReferenceHandler;
import spiralcraft.common.ContextualException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.ElementState;
import spiralcraft.textgen.OutputContext;
import spiralcraft.textgen.PrepareMessage;
import spiralcraft.textgen.RenderMessage;

/**
 * <p>Provides targets that allow child components to render to the prefix or suffix of the contents
 *   of this element
 * </p>
 * 
 * @author mike
 *
 */
public class Wrap
  extends Element
{ 
  
  class PrepareHandler
      extends AbstractMessageHandler
  {
    { this.type=PrepareMessage.TYPE;
    }

    @Override
    protected void doHandler(Dispatcher dispatcher, Message message,
            MessageHandlerChain next) 
    { 
      log.fine(dispatcher.getState().toString());
      ((WrapState) dispatcher.getState()).clear();
      next.handleMessage(dispatcher, message);
      
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
    { 
      WrapState wrapState=(WrapState) dispatcher.getState();
      if (wrapState==null)
      { 
        log.warning(getDeclarationInfo()+" No state");
        return;
      }
      try
      {
        OutputContext.get()
          .append( wrapState.header.toString() );
        next.handleMessage(dispatcher, message);
        OutputContext.get()
         .append( wrapState.footer.toString() );
      }
      catch (IOException x)
      { throw new RuntimeException("Error rendering content",x);
      }
    }
  }

  private final StateReferenceHandler<WrapState> stateRef
    =new StateReferenceHandler<>(WrapState.class);

  { addSelfFacet=true;
  }
    
  @Override
  protected void addHandlers()
    throws ContextualException
  { 
    super.addHandlers();
    addHandler(stateRef);
    addHandler(new PrepareHandler());
    addHandler(new RenderHandler());
  }
  
  public Appendable getHeader()
  { return stateRef.get().header;
  }
  
  public Appendable getFooter()
  { return stateRef.get().footer;
  }
  
  @Override
  public WrapState createState()
  { return new WrapState(this);
  }
}

class WrapState
  extends ElementState
{

  protected StringBuilder header=new StringBuilder();
  protected StringBuilder footer=new StringBuilder();

  WrapState(Wrap comp)
  { super(comp.getChildCount());
  }
  
  protected void clear() 
  {
    header.setLength(0);
    footer.setLength(0);
  }
  

}
