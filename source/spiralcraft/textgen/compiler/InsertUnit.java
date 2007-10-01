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

import java.io.IOException;

import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.text.markup.MarkupException;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;


import spiralcraft.text.xml.Attribute;

/**
 * A Unit which inserts the contents of an ancestral IncludeUnit
 */
public class InsertUnit
  extends ProcessingUnit
{
  
  
  public InsertUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,Attribute[] attribs
    )
    throws MarkupException
  { 
    super(parent);
    
    if (attribs!=null && attribs.length>0)
    {
      throw new MarkupException
        ("@insert does not accept attributes"
        ,compiler.getPosition()
        );
    }
    
  }
  
  
  public String getName()
  { return "@insert";
  }
  
  @Override
  public Element bind(Element parentElement)
    throws MarkupException
  {
    InsertElement element=new InsertElement();
    element.setParent(parentElement);
    try
    { element.bind(children);
    }
    catch (BindException x)
    { throw new MarkupException(x.toString(),getPosition());
    }
    
    return element;
  }
  
  
}

class InsertElement
  extends Element
{
  private IncludeElement ancestorInclude;
  
  @Override
  public void bind(List<TglUnit> children)
    throws BindException,MarkupException
  { 
    ancestorInclude=findElement(IncludeElement.class);
    super.bind(children);
  }
  
  @Override
  public void render(EventContext context)
    throws IOException
  { 
    if (ancestorInclude!=null)
    { ancestorInclude.renderClosure(context);
    }
    else 
    { renderChildren(context);
    }
    
  }

}