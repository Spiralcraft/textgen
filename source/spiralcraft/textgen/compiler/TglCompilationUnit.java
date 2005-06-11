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

import spiralcraft.text.markup.CompilationUnit;
import spiralcraft.text.markup.Unit;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.BuildException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;

import java.io.Writer;
import java.io.IOException;

/**
 * The root of a TGL compilation unit
 */
public class TglCompilationUnit
  extends CompilationUnit
  implements TglUnit
{
  public RootElement bind(Assembly parent,Focus focus)
    throws BuildException,BindException
  { 
    RootElement element=new RootElement();
    element.setFocus(focus);
    bindChildren(parent,element);
    return element;
  }
  
  public Element bind(Assembly parent,Element parentElement)
    throws BuildException,BindException
  { 
    Element element=new RootElement();
    element.bind(parentElement);
    bindChildren(parent,element);
    return element;
  }

  private void bindChildren(Assembly assembly,Element element)
    throws BuildException,BindException
  {
    Unit[] children=getChildren();
    if (children.length>0)
    { 
      Element[] childElements=new Element[children.length];
      for (int i=0;i<children.length;i++)
      { 
        
        if (children[i] instanceof TglUnit)
        { childElements[i]=((TglUnit) children[i]).bind(assembly,element);
        }
      }
      element.setChildren(childElements);
    }
  }
  
  class RootElement
    extends Element
  {
    private Focus _focus;
    
    public void setFocus(Focus focus)
    { _focus=focus;
    }
    
    public Focus getFocus()
    { 
      if (_focus!=null)
      { return _focus;
      }
      return super.getFocus();
    }
    
    public void write(Writer out)
      throws IOException
    { writeChildren(out);
    }
  }
}
