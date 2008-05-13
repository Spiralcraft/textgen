package spiralcraft.textgen.compiler;

import java.io.IOException;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;

public class NullElement
    extends Element
{

  @Override
  public void render(EventContext context) throws IOException
  { }
}
