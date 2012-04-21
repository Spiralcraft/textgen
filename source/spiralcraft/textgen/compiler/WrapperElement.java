package spiralcraft.textgen.compiler;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;

import spiralcraft.text.Wrapper;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.kit.WrapperHandler;

/**
 * <p>Holds a reference to Renderer for externally defined output. 
 * </p>
 * 
 * <p>The Renderer was created via spiralcraft.builder by the ElementFactory
 *   and is wrapped in the RenderElement before being bound.
 * </p>
 * 
 * @author mike
 *
 */
public class WrapperElement
  extends Element
{

  private final Wrapper wrapper;
  
  public WrapperElement(Wrapper wrapper)
  { this.wrapper=wrapper;
  }
  
  @Override
  protected Focus<?> bindStandard(Focus<?> focus) 
    throws ContextualException
  {
    addHandler(new WrapperHandler(wrapper));
    if (wrapper instanceof Contextual)
    { 
      Contextual fco=(Contextual) wrapper;
      focus=fco.bind(focus);
      
    }
    focus=focus.chain(getAssembly().getFocus().getSubject());
    return super.bindStandard(focus);
  }
  

  
}
