package spiralcraft.textgen.compiler;

import java.io.IOException;


import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;

import spiralcraft.text.Renderer;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;

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
  public Focus<?> bind(Focus<?> focus) 
    throws ContextualException
  { 
    
    if (renderer instanceof Contextual)
    { 
      Contextual fco=(Contextual) renderer;
      focus=fco.bind(focus);
      
    }
    focus=focus.chain(getAssembly().getFocus().getSubject());
    return super.bind(focus);
  }
  
  
  @Override
  public void render(EventContext context)
    throws IOException
  { 
    renderer.render(context.getOutput());
    renderChildren(context);
  }
  
}
