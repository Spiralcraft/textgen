package spiralcraft.textgen.compiler;


import spiralcraft.textgen.Element;

import spiralcraft.common.ContextualException;
import spiralcraft.common.namespace.PrefixResolver;

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
  protected Focus<?> bindStandard(Focus<?> focus)
    throws ContextualException
  { 
    
    focus=focus.chain(resolver);
    return super.bindStandard(focus);
    
  }

}

