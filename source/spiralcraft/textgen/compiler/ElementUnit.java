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

import spiralcraft.app.Parent;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.common.ContextualException;
import spiralcraft.common.namespace.NamespaceContext;
import spiralcraft.common.namespace.QName;
import spiralcraft.common.namespace.StandardPrefixResolver;
import spiralcraft.lang.Focus;

import spiralcraft.textgen.Element;

import spiralcraft.text.markup.MarkupException;

import spiralcraft.text.ParseException;


import spiralcraft.log.ClassLog;

/**
 * A Unit which represents an output Element delimited by start and end tag(s)
 *   or signified by an empty tag.
 */
public class ElementUnit
  extends MarkupUnit
{
  @SuppressWarnings("unused")
  private static final ClassLog log=ClassLog.getInstance(ElementUnit.class);
  

  
  private ElementFactory elementFactory;
  private URI elementPackage;
  private String elementName;
  private String skinName;
  private String instanceX;
  
  public ElementUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,CharSequence code
    )
    throws ParseException
  { 
    super(parent,code,compiler);

    QName name=resolvePrefixedName(getName(),TglUnit.DEFAULT_ELEMENT_PACKAGE);
    elementPackage=name.getNamespaceURI();
    elementName=name.getLocalName();
    
    if (!open)
    { close();
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
      ,new StandardPrefixResolver(getNamespaceResolver())
      );
    elementFactory.setInstanceX(instanceX);
    super.close();
  }

  

  
  @Override
  public Element bind(Focus<?> focus,Parent parentElement)
    throws ContextualException
  { 
    NamespaceContext.push(getNamespaceResolver());
    try
    { 
      
      Element element=elementFactory.createElement(focus,parentElement);

      // An element that has a skin should insert it around its children
      if (skinName!=null)
      {
        
        TglUnit skin=findDefinition(skinName);
        if (skin!=null)
        { 
          if (skin instanceof DefineUnit)
          { element.setSkin((DefineUnit) skin);
          }
          else
          { throw new MarkupException
              ("Skin '"+skinName+"' must be a Define",getPosition());
          }
        }
        else
        { 
          throw new MarkupException
          ("Skin '"+skinName+"' not defined",getPosition());
        }
      }
      return bind(focus,parentElement,element);
    
    }
    finally
    { NamespaceContext.pop();
    }
  }
  
  public AssemblyClass getAssemblyClass()
  { return elementFactory.getAssemblyClass();
  }
  
  @Override
  protected void addUnitAttribute(String name,String value)
    throws ParseException
  { 
    name=name.intern();
    if (name.equals("skin"))
    { skinName=value;
    }
    else if (name.equals("x"))
    { instanceX=value;
    }
    else
    { super.addUnitAttribute(name,value);
    }
    
  }


}
