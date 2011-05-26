package spiralcraft.textgen.elements;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.util.ExpressionRenderer;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.kit.RenderHandler;
import spiralcraft.util.string.StringConverter;

/**
 * <p>Output the result of an expression
 * </p>
 * 
 * @author mike
 *
 */
public class Out<T>
  extends Element
{ 
  private ExpressionRenderer<T> renderer
    =new ExpressionRenderer<T>();
  
  { addHandler(new RenderHandler(renderer));
  }

  /**
   * The expression to output
   * 
   * @param x
   */
  public void setX(Expression<T> x)
  { this.renderer.setX(x);
  }
  
  /**
   * The converter which translates this expression to text
   * @param converter
   */
  public void setConverter(StringConverter<T> converter)
  { this.renderer.setConverter(converter);
  }
  
  @Override
  public Focus<?> bind(Focus<?> focus)
    throws ContextualException
  { 
    renderer.bind(focus);
    return super.bind(focus);
  
  }
  
}
