package spiralcraft.textgen.compiler;


import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;

import spiralcraft.text.Renderer;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.kit.RenderHandler;

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
public class RendererElement
  extends Element
{

  private final Renderer renderer;
  
  public RendererElement(Renderer renderer)
  { this.renderer=renderer;
  }
  
  @Override
  protected Focus<?> bindStandard(Focus<?> focus) 
    throws ContextualException
  { 
    addHandler(new RenderHandler(renderer));
    
    if (renderer instanceof Contextual)
    { 
      Contextual fco=(Contextual) renderer;
      focus=fco.bind(focus);
      
    }
    focus=focus.chain(getAssembly().getFocus().getSubject());
    return super.bindStandard(focus);
  }

  
}
