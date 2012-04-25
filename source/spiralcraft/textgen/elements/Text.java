package spiralcraft.textgen.elements;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.text.MessageFormat;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.kit.RenderHandler;

/**
 * <p>Output the result of an expression
 * </p>
 * 
 * @author mike
 *
 */
public class Text
  extends Element
{ 
  protected MessageFormat format;
  
  /**
   * The expression to output
   * 
   * @param x
   */
  public void setFormat(MessageFormat format)
  { this.format=format;
  }
  
  public void setX(String text)
  { 
    try
    { this.format=new MessageFormat(text);
    }
    catch (Exception x)
    { throw new IllegalArgumentException("Error parsing "+text);
    }
  }
  
  @Override
  protected Focus<?> bindStandard(Focus<?> focus)
    throws ContextualException
  { 
    format.bind(focus);
    { addHandler(new RenderHandler(format));
    }

    return super.bindStandard(focus);
  }
  
}
