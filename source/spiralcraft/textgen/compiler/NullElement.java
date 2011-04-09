package spiralcraft.textgen.compiler;

import java.io.IOException;

import spiralcraft.lang.Focus;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;

public class NullElement
    extends Element
{

  @Override
  public Focus<?> bind(Focus<?> focus)
  { return focus;
  }
  
  @Override
  public void render(EventContext context) throws IOException
  { }
}
