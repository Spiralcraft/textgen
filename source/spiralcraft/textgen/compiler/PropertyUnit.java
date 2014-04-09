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


import java.util.List;

import spiralcraft.app.Parent;
import spiralcraft.common.namespace.PrefixResolver;
import spiralcraft.lang.Focus;
import spiralcraft.text.ParseException;

import spiralcraft.text.markup.MarkupException;

import spiralcraft.textgen.Element;


import spiralcraft.text.xml.Attribute;

/**
 * A Unit which defines a value for a property of a containing Element
 */
public class PropertyUnit
  extends MarkupUnit
{
  private String propertyName;

  public PropertyUnit
    (TglUnit parent
    ,CharSequence markup
    ,TglCompiler<?> compiler
    )
    throws MarkupException,ParseException
  { 
    super(parent,markup,compiler);

    readProperty();

    if (!open)
    { close();
    }
    for (Attribute attribute: attributes)
    { super.checkUnitAttribute(attribute);
    }
    
  }
  
  @Override
  public Attribute getAttribute(String name)
  {
    for (Attribute attribute: attributes)
    { 
      if (attribute.getName().equals(name))
      { return attribute;
      }
    }
    return null;
  }
  
  
  /**
   * An element with a tag name in the form <code>&lt;%namespace:name ... %&gt;</code> 
   * 
   * @throws ParseException
   */
  private void readProperty()
    throws ParseException
  { propertyName=getName().substring(1);
  }  
  

  public String getPropertyName()
  { return propertyName;
  }  
  
  /**
   * <p>Notify PropertyUnit of a close tag.
   * </p>
   * 
   * <p>Provides an opportunity for an PropertyUnit to 
   *   integrate its content.
   * </p>
   *   
   */
  @Override
  public void close()
    throws MarkupException
  {
    open=false;
    try
    { getParent().addProperty(this);
    }
    catch (IllegalArgumentException x)
    {
      throw new MarkupException
        ("Cannot assign property '"+propertyName+"' to containing"
        +" element."
        ,getPosition().clone()
        ,x
        );
    }
    super.close();
            
        
  }
  
  @Override
  protected Element createElement()
  { return new PropertyElement();
  }

  @Override
  public Element bindExtension(
    Attribute[] attribs,
    Focus<?> focus,
    Parent parentElement,
    List<TglUnit> children
    ,PrefixResolver attributePrefixResolver
    )
    throws MarkupException
  {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public String toString()
  { return super.toString()+": "+propertyName;
  }
  
  
}

