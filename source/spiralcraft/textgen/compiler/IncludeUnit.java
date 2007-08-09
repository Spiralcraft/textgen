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

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import spiralcraft.text.markup.MarkupException;
import spiralcraft.text.ParseException;

import spiralcraft.textgen.Element;

import spiralcraft.xml.Attribute;

/**
 * A Unit which includes another file
 */
public class IncludeUnit
  extends ProcessingUnit
{
  
  private DocletUnit docletUnit;
  
  public String getName()
  { return "@include";
  }
  
  public IncludeUnit(TglUnit parent,TglCompiler compiler,Attribute[] attribs)
    throws MarkupException
  { 
    super(parent);
    allowsChildren=false;
    
    for (Attribute attrib: attribs)
    {
      if (attrib.getName().equals("resource"))
      {
        URI resourceURI=null;
        try
        { resourceURI=new URI(attrib.getValue());
        }
        catch (URISyntaxException x)
        { 
          throw new MarkupException
            ("Error creating URI '"+attrib.getValue()+"':"+x
            ,compiler.getPosition()
            );
        }
        
        
        if (!resourceURI.isAbsolute())
        {
          DocletUnit parentDoc=findUnit(DocletUnit.class);
          URI baseURI=parentDoc.getSourceURI();
          resourceURI=baseURI.resolve(resourceURI);
          
        }
        try
        { docletUnit=compiler.subCompile(this,resourceURI);
        }
        catch (ParseException x)
        { 
          throw new MarkupException
            ("Error including URI '"+attrib.getValue()+"':"+x
            ,compiler.getPosition()
            );
        }
        catch (IOException x)
        {
          throw new MarkupException
            ("Error including URI '"+attrib.getValue()+"':"+x
            ,compiler.getPosition()
            );
        }
      }
      else
      { 
        throw new MarkupException
          ("Attribute '"+attrib.getName()+"' not in {resource}"
          ,compiler.getPosition()
          );
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
  
  public Element bind(Element parentElement)
    throws MarkupException
  { return docletUnit.bind(parentElement.getFocus());
  }
  
}
