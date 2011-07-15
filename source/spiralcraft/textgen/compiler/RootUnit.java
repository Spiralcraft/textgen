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

import spiralcraft.textgen.Element;

import spiralcraft.text.ParseException;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.text.xml.Attribute;

/**
 * 
 * 
 */
public class RootUnit
  extends ProcessingUnit
{
  private final DocletUnit docletUnit;
  
  public RootUnit(TglUnit parent,TglCompiler<?> compiler,Attribute[] attributes)
    throws MarkupException,ParseException
  { 
    super(parent,compiler);

    if (!(parent instanceof DocletUnit))
    { throw new MarkupException
        ("@doclet unit cannot be used here",getPosition()); 
    }
    docletUnit=(DocletUnit) parent;
    if (attributes!=null)
    { 
      for (Attribute attrib:attributes)
      { checkUnitAttribute(attrib);
      }
      docletUnit.setAttributes(attributes);
    }
    
  }
  
  @Override
  public void addProperty(PropertyUnit property)
  { docletUnit.addProperty(property);
  }
  
  @Override
  public String getName()
  { return "@doclet";
  }

  
  @Override
  public Element createElement()
  { return new RootElement();
  }
  
    
  class RootElement
    extends Element
  {
  }
  
}
