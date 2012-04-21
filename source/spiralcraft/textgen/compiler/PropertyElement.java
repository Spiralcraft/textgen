package spiralcraft.textgen.compiler;


import spiralcraft.lang.Focus;
import spiralcraft.textgen.Element;


public class PropertyElement
    extends Element
{

  @Override
  protected Focus<?> bindStandard(Focus<?> focus)
  { 
    // Stop binding here, rest of tree does not contain elements
    return focus;
  }

}
