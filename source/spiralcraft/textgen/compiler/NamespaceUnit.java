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
import spiralcraft.lang.NamespaceResolver;
import spiralcraft.log.ClassLogger;

import spiralcraft.textgen.Element;

import spiralcraft.text.xml.Attribute;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.HashMap;

import spiralcraft.text.markup.MarkupException;

/**
 * A Unit which defines namespaces
 */
public class NamespaceUnit
  extends ProcessingUnit
{
  private static final ClassLogger log
    =ClassLogger.getInstance(NamespaceUnit.class);
  
  private HashMap<String,URI> map
    =new HashMap<String,URI>();
  private final NamespaceUnit parentNamespaceUnit;
  
  public NamespaceUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,Attribute[] attribs
    )
    throws MarkupException
  { 
    super(parent);
    parentNamespaceUnit=parent.findUnit(NamespaceUnit.class);
    
    for (Attribute attrib: attribs)
    {
      try
      { 
        URI uri=new URI(attrib.getValue());
        if (!uri.isAbsolute())
        { uri=compiler.getPosition().getContextURI().resolve(uri);
        }
        map.put(attrib.getName(),uri);
      }
      catch (URISyntaxException x)
      { 
        throw new MarkupException
          ("Error creating URI '"+attrib.getValue()+"':"+x
          ,compiler.getPosition()
          );
      }
    }
  }
  
  public String getName()
  { return "@namespace";
  }
  
  public URI resolveNamespace(String namespaceId)
  { 
    URI namespace=map.get(namespaceId);
    if (namespace!=null)
    { return namespace;
    }
    else if (parentNamespaceUnit!=null)
    { 
      namespace=parentNamespaceUnit.resolveNamespace(namespaceId);
      if (namespace==null && namespaceId.equals("default"))
      { namespace=ElementUnit.DEFAULT_ELEMENT_PACKAGE;
      }
      return namespace;
    }
    
    return null;
  }
  
  
  @Override
  public Element bind(Element parentElement)
    throws MarkupException
  {
    NamespaceElement element
      =new NamespaceElement
        (new NamespaceResolver()
        {

          @Override
          public URI getDefaultNamespaceURI()
          { 
            return ElementUnit.DEFAULT_ELEMENT_PACKAGE;
            // return NamespaceUnit.this.resolveNamespace("default");
          }

          @Override
          public URI resolveNamespace(String prefix)
          { 
            log.fine("resolveNamespace "+prefix);
            return NamespaceUnit.this.resolveNamespace(prefix);
          }
        }
        );
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
