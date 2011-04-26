package spiralcraft.textgen.compiler;

import spiralcraft.textgen.Element;

/**
 * <p>Holds a reference to single POJO used for a parameter value 
 * </p>
 * 
 * @author mike
 *
 */
public class ObjectElement
    extends Element
{

  private final Object object;
  
  public ObjectElement(Object object)
  { this.object=object;
  }
  
  public Object getObject()
  { return object;
  }

}
