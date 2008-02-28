package spiralcraft.textgen.compiler;

import java.io.IOException;
import java.util.List;

import spiralcraft.text.markup.MarkupException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;

import spiralcraft.lang.BindException;
import spiralcraft.lang.NamespaceResolver;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.FocusWrapper;

import spiralcraft.log.ClassLogger;

public class NamespaceElement
  extends Element
{
  @SuppressWarnings("unused")
  private static final ClassLogger log=new ClassLogger(NamespaceElement.class);
  
  private NamespaceResolver resolver;
  private Focus<?> focus;
  
  public NamespaceElement(NamespaceResolver resolver)
  { this.resolver=resolver;
  }

  @Override
  public Focus<?> getFocus()
  { 
    // log.fine("XXX "+focus.toString());
    return focus;
  }
  
  @Override
  public void render(EventContext context) throws IOException
  { 
    renderChildren(context);
  }
  
  @Override
  @SuppressWarnings("unchecked") // Not using generic versions
  public void bind(List<TglUnit> childUnits)
    throws BindException,MarkupException
  { 
    Focus<?> parentFocus=getParent().getFocus();
    focus=new FocusWrapper(parentFocus)
      {
        public NamespaceResolver getNamespaceResolver()
        { 
          // log.fine("XXX "+resolver.toString());
          return resolver;
        }
      };
    super.bind(childUnits);
    
  }

}

