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


import spiralcraft.common.NamespaceResolver;

import spiralcraft.textgen.Element;

import spiralcraft.text.ParseException;
import spiralcraft.text.xml.Attribute;

import java.net.URI;
import java.net.URISyntaxException;


import spiralcraft.text.markup.MarkupException;

/**
 * A Unit which defines namespaces
 */
public class NamespaceUnit
  extends ProcessingUnit
{
  private TglPrefixResolver prefixResolver;

  
  public NamespaceUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,Attribute[] attribs
    )
    throws MarkupException
  { 
    super(parent);
    prefixResolver=new TglPrefixResolver(parent.getNamespaceResolver());
      
    for (Attribute attrib: attribs)
    {
      
      
      try
      { 
        if (checkUnitAttribute(attrib))
        { continue;
        }
        
        URI uri=new URI(attrib.getValue());
        if (!uri.isAbsolute())
        { uri=compiler.getPosition().getContextURI().resolve(uri);
        }
        prefixResolver.mapPrefix(attrib.getName(),uri);
      }
      catch (URISyntaxException x)
      { 
        throw new MarkupException
          ("Error creating URI '"+attrib.getValue()+"'"
          ,compiler.getPosition()
          ,x
          );
      }
      catch (ParseException x)
      { 
        throw new MarkupException
          ("Error parsing attribute '"+attrib.getName()+"'"
          ,compiler.getPosition()
          ,x
          );
      }
    }
  }
  
  @Override
  public String getName()
  { return "@namespace";
  }
   
  @Override
  public NamespaceResolver getNamespaceResolver()
  { 
    if (prefixResolver!=null)
    { return prefixResolver;
    }
    else 
    { return super.getNamespaceResolver();
    }
  }
    
  @Override
  public Element bind(Element parentElement)
    throws MarkupException
  {
    NamespaceElement element
      =new NamespaceElement
        (getNamespaceResolver());
    
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
