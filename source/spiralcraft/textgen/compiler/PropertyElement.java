package spiralcraft.textgen.compiler;


import spiralcraft.lang.Focus;
import spiralcraft.textgen.Element;


public class PropertyElement
    extends Element
{

  @Override
  public Focus<?> bind(Focus<?> focus)
  { 
    // Stop binding here, rest of tree does not contain elements
    return focus;
  }

}
