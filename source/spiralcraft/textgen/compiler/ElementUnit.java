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

import spiralcraft.builder.AssemblyClass;
import spiralcraft.common.namespace.PrefixResolver;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;

import spiralcraft.textgen.Element;

import spiralcraft.text.markup.MarkupException;

import spiralcraft.text.ParseException;
import spiralcraft.text.ParsePosition;

import spiralcraft.util.ArrayUtil;

import spiralcraft.log.ClassLog;

/**
 * A Unit which represents an output Element delimited by start and end tag(s)
 *   or signified by an empty tag.
 */
public class ElementUnit
  extends MarkupUnit
{
  private static final ClassLog log=ClassLog.getInstance(ElementUnit.class);
  
  public static final URI DEFAULT_ELEMENT_PACKAGE
    =URI.create("class:/spiralcraft/textgen/elements/");
  
  private final TglCompiler<?> compiler;
  private ElementFactory elementFactory;
  private URI elementPackage;
  private String elementName;
  private PropertyUnit[] properties;
  
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
      PrefixResolver resolver
        =getNamespaceResolver();
      
      elementPackage
        = resolver!=null
        ? resolver.resolvePrefix(name.substring(0,nspos))
        : null
        ;
           
      if (elementPackage==null)
      { 
        throw new ParseException
          ("Namespace prefix '"+name.substring(0,nspos)+"' not found"
          ,getPosition()
          );
      }
      
      elementName=name.substring(nspos+1);
    }
    else
    { 
      elementPackage=DEFAULT_ELEMENT_PACKAGE;
      elementName=name;
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
  @Override
  public void close()
    throws MarkupException
  {
    open=false;
    // We're creating a standard Element
    elementFactory
      =compiler.createElementFactory
      (elementPackage
      ,elementName
      ,attributes
      ,properties
      ,getPosition()
      );
  }

  
  public void addProperty
    (PropertyUnit propertyUnit)
  {
    if (properties==null)
    { properties=new PropertyUnit[0];
    }
    properties=ArrayUtil
      .append(properties,propertyUnit);
    if (debug)
    { log.fine("Added property "+propertyUnit.getPropertyName());
    }
  }
  
  @Override
  public Element bind(Focus<?> focus,Element parentElement)
    throws MarkupException
  { 

    Element element=elementFactory.createElement(focus,parentElement);
    try
    { element.bind(focus,children);
    }
    catch (BindException x)
    { throw new MarkupException(x.toString(),getPosition(),x);
    }
    return element;
  }
  
  public AssemblyClass getAssemblyClass()
  { return elementFactory.getAssemblyClass();
  }

}
