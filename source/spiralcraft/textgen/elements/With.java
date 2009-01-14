package spiralcraft.textgen.elements;

import spiralcraft.textgen.ExpressionFocusElement;


/**
 * <p>Creates a new Focus for the result of an expression evaluation and 
 *   the computed value for repeated access by this element's children
 *   during a render or message cycle.
 * </p>
 * 
 * <p>By default, this element will invalidate before prepare and after 
 *   rendering. If data is valid between rendering and a future prepare
 *   stage, set invalidateAfterRender=false
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class With<T>
  extends ExpressionFocusElement<T>
{
  { setInvalidateAfterRender(true);
  }
  
}
