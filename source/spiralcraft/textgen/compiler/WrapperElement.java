package spiralcraft.textgen.compiler;

import java.io.IOException;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;

import spiralcraft.text.Renderer;
import spiralcraft.text.Wrapper;

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
public class WrapperElement
  extends Element
{

  private final Wrapper wrapper;
  
  public WrapperElement(Wrapper wrapper)
  { this.wrapper=wrapper;
  }
  
  @Override
  public Focus<?> bind(Focus<?> focus) 
    throws ContextualException
  {
    if (wrapper instanceof Contextual)
    { 
      Contextual fco=(Contextual) wrapper;
      focus=fco.bind(focus);
      
    }
    focus=focus.chain(getAssembly().getFocus().getSubject());
    return super.bind(focus);
  }
  

  
  @Override
  public void render(final EventContext context)
    throws IOException
  { 
    wrapper.render
      (context.getOutput()
      ,new Renderer()
      {
        @Override
        public void render(Appendable writer)
          throws IOException
        { renderChildren(context);
        }
      }
      );
  }
  
}
