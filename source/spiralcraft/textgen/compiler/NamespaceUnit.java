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
  
  private HashMap<String,URI> map
    =new HashMap<String,URI>();
  
  public NamespaceUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,Attribute[] attribs
    )
    throws MarkupException
  { 
    super(parent);
    
    for (Attribute attrib: attribs)
    {
      try
      { map.put(attrib.getName(),new URI(attrib.getValue()));
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
  { return map.get(namespaceId);
  }
  
  public Element bind(Element parentElement)
    throws MarkupException
  { return defaultBind(parentElement);
  }
  
}
