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
import spiralcraft.text.ParsePosition;

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
    ,ParsePosition position
    )
    throws MarkupException,ParseException
  { 
    super(parent,markup,position);

    readProperty();

    if (!open)
    { close();
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
    if (getParent() instanceof ElementUnit)
    {
      ((ElementUnit) getParent())
        .addProperty(this);
    }
    else
    { 
      throw new MarkupException
        ("Cannot assign property '"+propertyName+"' to containing"
        +" element."
        ,getPosition().clone()
        );
    }
            
        
  }
  
  @Override
  public Element bind(Element parentElement)
    throws MarkupException
  { 
    // Properties don't have Elements that output anything directly
    Element element=new NullElement();
    try
    { element.bind(children);
    }
    catch (BindException x)
    { throw new MarkupException(x.toString(),getPosition(),x);
    }
    return element;
  }
  
}

