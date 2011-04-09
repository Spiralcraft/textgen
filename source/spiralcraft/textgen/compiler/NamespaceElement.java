package spiralcraft.textgen.compiler;

import java.io.IOException;

import spiralcraft.text.markup.MarkupException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;

import spiralcraft.common.namespace.PrefixResolver;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;

import spiralcraft.log.ClassLog;

public class NamespaceElement
  extends Element
{
  @SuppressWarnings("unused")
  private static final ClassLog log=ClassLog.getInstance(NamespaceElement.class);
  
  private PrefixResolver resolver;
  
  public NamespaceElement(PrefixResolver resolver)
  { this.resolver=resolver;
  }

  
  @Override
  public void render(EventContext context) throws IOException
  { 
    renderChildren(context);
  }
  

  @Override
  public Focus<?> bind(Focus<?> focus)
    throws BindException,MarkupException
  { 
    
    focus=focus.chain(resolver);
    return super.bind(focus);
    
  }

}

