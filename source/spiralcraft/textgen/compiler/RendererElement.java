package spiralcraft.textgen.compiler;

import java.io.IOException;
import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusChainObject;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.text.Renderer;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;

/**
 * <p>Holds a reference to Renderer for externally defined output
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
    if (renderer instanceof FocusChainObject)
    { 
      FocusChainObject fco=(FocusChainObject) renderer;
      fco.bind(focus);
      focus=fco.getFocus();
      
    }
    focus=focus.chain(new SimpleChannel<RendererElement>(this,true));
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
    renderChildren(context);
    renderer.render(context.getWriter());
  }
}
