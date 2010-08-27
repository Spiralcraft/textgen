package spiralcraft.textgen.compiler;

import java.io.IOException;
import java.util.List;

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
  
  public NamespaceElement(Element parent,PrefixResolver resolver)
  { 
    super(parent);
    this.resolver=resolver;
  }

  
  @Override
  public void render(EventContext context) throws IOException
  { 
    renderChildren(context);
  }
  
  @Override
  public void bind(Focus<?> focus,List<TglUnit> childUnits)
    throws BindException,MarkupException
  { 
    
    focus=focus.chain(resolver);
    super.bind(focus,childUnits);
    
  }

}

