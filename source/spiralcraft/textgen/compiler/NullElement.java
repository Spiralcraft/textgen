package spiralcraft.textgen.compiler;


import spiralcraft.lang.Focus;
import spiralcraft.textgen.Element;

public class NullElement
    extends Element
{

  @Override
  public Focus<?> bind(Focus<?> focus)
  { return focus;
  }
  

}
