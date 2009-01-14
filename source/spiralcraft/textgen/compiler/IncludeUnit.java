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


import spiralcraft.lang.BindException;

import spiralcraft.text.ParseException;
import spiralcraft.text.markup.MarkupException;

import spiralcraft.textgen.Element;

import spiralcraft.text.xml.Attribute;

/**
 * A Unit which includes another file
 */
public class IncludeUnit
  extends ProcessingUnit
{
  
  
  @Override
  public String getName()
  { return "@include";
  }
  
  public IncludeUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,Attribute[] attribs
    )
    throws MarkupException
  { 
    super(parent);
    allowsChildren=true;

    DocletUnit docletUnit=null;

    for (Attribute attrib: attribs)
    {
      
      try
      {
        if (attrib.getName().equals("resource"))
        { docletUnit=includeResource(attrib.getValue(),compiler);
        }
        else if (!checkUnitAttribute(attrib))
        { 
          throw new MarkupException
            ("Attribute '"+attrib.getName()+"' not in {resource}"
            ,compiler.getPosition()
            );
        }
      }
      catch (ParseException x)
      { 
        throw new MarkupException
          ("Error reading attribute",compiler.getPosition(),x);
      }
      
    }
    
    if (docletUnit==null)
    {
      throw new MarkupException
        ("Required attribute 'resource' not found in @include"
        ,compiler.getPosition()
        );
    }
  }
  
  @Override
  public Element bind(Element parentElement)
    throws MarkupException
  {
    IncludeElement includeElement=new IncludeElement();
    includeElement.setParent(parentElement);
    
    try
    { includeElement.bind(children);
    }
    catch (BindException x)
    { throw new MarkupException(x.toString(),getPosition());
    }
    
    return includeElement;
  }


}
