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
import spiralcraft.textgen.GenerationContext;


import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;

import java.io.IOException;

import java.net.URI;


/**
 * A compilation unit (ie. a file or other container) of tgl markup.
 */
public class TglCompilationUnit
  extends TglUnit
{
  private final URI sourceURI;
  
  public TglCompilationUnit(URI sourceURI)
  { this.sourceURI=sourceURI;
  }
  
  public Element bind(Focus focus)
    throws BindException
  {
    RootElement element=new RootElement();
    element.setFocus(focus);
    element.bind(null,children);
    return element;
    
  }
  
  public Element bind(Element parentElement)
    throws BindException
  { 
    Element element=new RootElement();
    element.bind(parentElement,children);
    return element;
  }
  
  class RootElement
    extends Element
  {
    private Focus _focus;
    
    public void setFocus(Focus focus)
    { _focus=focus;
    }
    
    public URI getContextURI()
    { return sourceURI;
    }
    
    public Focus getFocus()
    { 
      if (_focus!=null)
      { return _focus;
      }
      return super.getFocus();
    }
    
    public void write(GenerationContext context)
      throws IOException
    { writeChildren(context);
    }
  }
}
