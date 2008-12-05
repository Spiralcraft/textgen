package spiralcraft.textgen.compiler;

import java.io.IOException;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;

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
  
  @Override
  public void render(EventContext context) throws IOException
  { }
}
