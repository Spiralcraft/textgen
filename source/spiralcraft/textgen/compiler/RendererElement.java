package spiralcraft.textgen.compiler;

import java.io.IOException;

import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;

import spiralcraft.text.Renderer;
import spiralcraft.text.markup.MarkupException;

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
  private Focus<?> focus;
  
  public RendererElement(Renderer renderer)
  { this.renderer=renderer;
  }
  
  @Override
  public void bind(List<TglUnit> childUnits) 
    throws MarkupException, BindException 
  { 
    focus=getParent().getFocus();
    
    if (renderer instanceof Contextual)
    { 
      Contextual fco=(Contextual) renderer;
      focus=fco.bind(focus);
      
    }
    focus=focus.chain(getAssembly().getFocus().getSubject());
    super.bind(childUnits);
  }
  
  @Override
  public Focus<?> getFocus()
  { return focus;
  }
  
  @Override
  public void render(EventContext context)
    throws IOException
  { 
    renderer.render(context.getWriter());
    renderChildren(context);
  }
  
}
