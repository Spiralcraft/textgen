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


import java.net.URI;

import spiralcraft.lang.BindException;

import spiralcraft.textgen.Element;

import spiralcraft.text.markup.MarkupException;

import spiralcraft.text.ParseException;
import spiralcraft.text.ParsePosition;

import spiralcraft.util.ArrayUtil;

import spiralcraft.log.ClassLogger;

/**
 * A Unit which represents an output Element delimited by start and end tag(s)
 *   or signified by an empty tag.
 */
public class ElementUnit
  extends MarkupUnit
{
  private static final ClassLogger log=ClassLogger.getInstance(ElementUnit.class);
  
  public static final URI DEFAULT_ELEMENT_PACKAGE
    =URI.create("class:/spiralcraft/textgen/elements/");
  
  private final TglCompiler<?> compiler;
  private ElementFactory elementFactory;
  private URI elementPackage;
  private String elementName;
  private PropertyUnit[] properties;
  private boolean debug;
  
  public ElementUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,CharSequence code
    ,ParsePosition position
    )
    throws ParseException
  { 
    super(parent,code,position);
    this.compiler=compiler;
    readStandardElement();
    if (!open)
    { close();
    }
  }
  
  
  /**
   * An element with a tag name in the form <code>&lt;%namespace:name ... %&gt;</code> 
   * 
   * @throws ParseException
   */
  private void readStandardElement()
    throws ParseException
  { 
    String name=getName();
    int nspos=name.indexOf(':');
    if (nspos>-1)
    {  
      elementPackage=resolveNamespace(name.substring(0,nspos));
      elementName=name.substring(nspos+1);
    }
    else
    { 
      elementPackage=DEFAULT_ELEMENT_PACKAGE;
      elementName=name;
    }
  }
  
  
  private URI resolveNamespace(String namespaceId)
    throws MarkupException
  {
    // Called via the constructor
    
    URI namespaceURI=null;
    NamespaceUnit unit=this.findUnit(NamespaceUnit.class);
    if (unit!=null)
    { namespaceURI=unit.resolveNamespace(namespaceId);
    }
    if (namespaceURI==null)
    { throw new MarkupException("Unknown namespace "+namespaceId,getPosition());
    }
    else
    { return namespaceURI;
    }
  }

  
  /**
   * <p>Notify ElementUnit of a close tag.
   * </p>
   * 
   * <p>Provides an opportunity for an ElementUnit to 
   *   integrate its content.
   * </p>
   *   
   */
  public void close()
    throws MarkupException
  {
    open=false;
    // We're creating a standard Element
    elementFactory=compiler.createElementFactory
      (elementPackage
      ,elementName
      ,attributes
      ,properties
      ,getPosition()
      );
  }

  
  public void addProperty
    (PropertyUnit propertyUnit)
    throws MarkupException
  {
    if (properties==null)
    { properties=new PropertyUnit[0];
    }
    properties=(PropertyUnit[]) ArrayUtil
      .append(properties,propertyUnit);
    if (debug)
    { log.fine("Added property "+propertyUnit.getPropertyName());
    }
  }
  
  public Element bind(Element parentElement)
    throws MarkupException
  { 

    Element element=elementFactory.createElement(parentElement);
    try
    { element.bind(children);
    }
    catch (BindException x)
    { throw new MarkupException(x.toString(),getPosition(),x);
    }
    return element;
  }

}
