package spiralcraft.textgen.compiler;

import java.io.IOException;

import spiralcraft.common.ContextualException;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.State;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.OutputContext;
import spiralcraft.textgen.kit.RenderHandler;

public class TextElement
    extends Element
{
  private final String elementContent;
    
  @Override
  protected void addHandlers()
    throws ContextualException
  {
    addHandler
      (new RenderHandler() 
        {
          @Override
          protected void render(Dispatcher context)
            throws IOException
          { 
            if (elementContent!=null)
            { OutputContext.get().append(elementContent);
            }              
          }
        } 
      );
    super.addHandlers();
  }
  
  public TextElement(String content)
  { this.elementContent=content;
  }
    
  @Override
  public State createState()
  { return new TextState();
  }
}
