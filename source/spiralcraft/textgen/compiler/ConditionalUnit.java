//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.textgen.compiler;

import spiralcraft.common.ContextualException;
import spiralcraft.common.namespace.NamespaceContext;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.ParseException;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.textgen.Element;
import spiralcraft.util.ContextDictionary;


/**
 * A Unit which only binds its contents when the value of a constant expression
 *   returns true.
 */
public class ConditionalUnit
  extends ProcessingUnit
{
  
  private final Expression<Boolean> code;
  
  public ConditionalUnit
    (TglUnit parent
    ,CharSequence code
    ,TglCompiler<?> compiler
    )
    throws MarkupException
  { 
    super(parent,compiler);
    NamespaceContext.push(getNamespaceResolver());
    try
    { this.code=Expression.<Boolean>parse(ContextDictionary.substitute(code.toString().substring(1)));
    }
    catch (spiralcraft.text.ParseException x)
    {       
      throw new MarkupException
        ("Error parsing contextual substitution",compiler.getPosition(),x);
    }
    catch (ParseException x)
    {
      throw new MarkupException
        ("Error parsing conditional expression",compiler.getPosition(),x);
    }
    finally
    { NamespaceContext.pop();
    }
  }
  
  
  @Override
  public String getName()
  { return "?";
  }
  
  @Override
  public Element createElement()
  { return new ConditionalElement(code);
  }
  
  
}

class ConditionalElement
  extends Element
{
  private Binding<Boolean> condition;
  
  public ConditionalElement(Expression<Boolean> code)
  { condition=new Binding<Boolean>(code);
  }
  
  @Override
  protected Focus<?> bindStandard(Focus<?> parentFocus)
    throws ContextualException
  {
    condition.bind(parentFocus);
    
    if (!condition.isConstant())
    { throw new ContextualException("Condition must be constant");
    }
    if (Boolean.TRUE.equals(condition.get()))
    { return super.bindStandard(parentFocus);
    }
    else
    { return parentFocus;
    }
  }
}