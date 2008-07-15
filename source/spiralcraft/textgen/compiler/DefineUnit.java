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

import spiralcraft.lang.BindException;
import spiralcraft.text.markup.MarkupException;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;


import spiralcraft.text.xml.Attribute;

/**
 * A Unit which defines a subtree or a value for insertion elsewhere.
 */
public class DefineUnit
  extends ProcessingUnit
{
  
  private String publishedName;
  
  public DefineUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,Attribute[] attribs
    )
    throws MarkupException
  { 
    super(parent);
    allowsChildren=true;
    
    for (Attribute attrib: attribs)
    {
      if (attrib.getName().equals("name"))
      { this.publishedName=attrib.getValue();
      }
      else if (attrib.getName().equals("resource"))
      { includeResource(attrib.getValue(),compiler);
      }
      else
      { 
        throw new MarkupException
          ("Attribute '"+attrib.getName()+"' not in {resource,value}"
          ,compiler.getPosition()
          );
      }
    }
    
    if (publishedName==null)
    { throw new MarkupException
        ("Attribute 'name' required for @define",compiler.getPosition());
    }
    parent.define(publishedName,this);
    

  }
  
  public String getPublishedName()
  { return publishedName;
  }  
  
  public String getName()
  { return "@define";
  }
  
  @Override
  /**
   * The DefineUnit does not bind into it's container. Binding is deferred
   *   so the content can be bound into the referencing Insert unit.
   */
  public Element bind(Element parentElement)
    throws MarkupException
  { 
    NullElement ret=new NullElement();
    try
    { ret.bind(null);
    }
    catch (BindException x)
    { throw new MarkupException("Error binding null element",getPosition(),x);
    }
    return ret;
    
  }
  
  public Element bindContent(Element parentElement)
    throws MarkupException
  {
    DefineElement element=new DefineElement();
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

class DefineElement
  extends Element
{
  
  @Override
  public void render(EventContext context)
    throws IOException
  { renderChildren(context);
  }

}