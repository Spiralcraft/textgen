package spiralcraft.textgen.compiler;

import java.io.IOException;

import spiralcraft.lang.Focus;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;

import java.util.List;

public class PropertyElement
    extends Element
{
  
  public PropertyElement(Element parent)
  { super(parent);
  }

  @Override
  public void bind(Focus<?> focus,List<TglUnit> childUnits)
  { 
    // Stop binding here, rest of tree does not contain elements
  }
  
  @Override
  public void render(EventContext context) throws IOException
  { }
}
