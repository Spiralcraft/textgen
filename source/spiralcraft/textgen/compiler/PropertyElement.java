package spiralcraft.textgen.compiler;

import java.io.IOException;

import spiralcraft.lang.Focus;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;


public class PropertyElement
    extends Element
{

  @Override
  public Focus<?> bind(Focus<?> focus)
  { 
    // Stop binding here, rest of tree does not contain elements
    return focus;
  }
  
  @Override
  public void render(EventContext context) throws IOException
  { }
}
