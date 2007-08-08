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
import spiralcraft.textgen.RenderingContext;

import spiralcraft.text.markup.MarkupException;

import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

import java.io.IOException;

import java.net.URI;


/**
 * A compilation unit (ie. a file or other container) of tgl markup.
 * 
 * XXX The name "Doclet" needs to be refer to this organizational unit
 */
public class DocletUnit
  extends TglUnit
{
  protected final URI sourceURI;
  
  public DocletUnit(URI sourceURI)
  { this.sourceURI=sourceURI;
  }
  
  public Element bind(Focus<?> focus)
    throws MarkupException
  {
    RootElement element=new RootElement();
    element.setFocus(focus);
    
    try
    { element.bind(null,children);
    }
    catch (BindException x)
    { throw new MarkupException(x.toString(),getPosition());
    }
    return element;
    
  }
  
  public Element bind(Element parentElement)
    throws MarkupException
  { 
    Element element=new RootElement();
    try
    { element.bind(parentElement,children);
    }
    catch (BindException x)
    { throw new MarkupException(x.toString(),getPosition());
    }
    
    return element;
  }
  
  class RootElement
    extends Element
  {
    private Focus<?> _focus;
    
    public void setFocus(Focus<?> focus)
    { _focus=focus;
    }
    
    public URI getContextURI()
    { return sourceURI;
    }
    
    public Focus<?> getFocus()
    { 
      if (_focus!=null)
      { return _focus;
      }
      return super.getFocus();
    }
    
    public void write(RenderingContext context)
      throws IOException
    { writeChildren(context);
    }
  }
}
