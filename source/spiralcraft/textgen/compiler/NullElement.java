package spiralcraft.textgen.compiler;


import spiralcraft.lang.Focus;
import spiralcraft.textgen.Element;

public class NullElement
    extends Element
{

  @Override
  protected Focus<?> bindStandard(Focus<?> focus)
  { return focus;
  }
  

}
