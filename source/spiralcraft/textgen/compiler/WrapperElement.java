package spiralcraft.textgen.compiler;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusChainObject;

import spiralcraft.text.Renderer;
import spiralcraft.text.Wrapper;
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
public class WrapperElement
  extends Element
{

  private final Wrapper wrapper;
  private Focus<?> focus;
  
  public WrapperElement(Wrapper wrapper)
  { this.wrapper=wrapper;
  }
  
  @Override
  public void bind(List<TglUnit> childUnits) 
    throws MarkupException, BindException 
  { 
    focus=getParent().getFocus();
    
    if (wrapper instanceof FocusChainObject)
    { 
      FocusChainObject fco=(FocusChainObject) wrapper;
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
  public void render(final EventContext context)
    throws IOException
  { 
    wrapper.render
      (context.getWriter()
      ,new Renderer()
      {
        public void render(Writer writer)
          throws IOException
        { renderChildren(context);
        }
      }
      );
  }
  
}