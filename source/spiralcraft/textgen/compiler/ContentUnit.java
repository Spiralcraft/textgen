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
import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;


/**
 * A Unit which contains literal text
 */
public class ContentUnit
  extends TglUnit
{
  private final CharSequence content;
  
  public ContentUnit(TglUnit parent,CharSequence content)
  { 
    super(parent);
    
    this.content=content;
  }
  
  @Override
  public Element bind(Element parentElement)
    throws MarkupException
  { 
    TextElement ret=new TextElement();
    ret.setParent(parentElement);
    try
    { ret.bind(children);
    }
    catch (BindException x)
    { throw new MarkupException(x.toString(),getPosition(),x);
    }
    return ret;
  }
  
  public CharSequence getContent()
  { return content;
  }
  
  class TextElement
    extends Element
  {
    private CharSequence elementContent;
    
    @Override
    public void bind(List<TglUnit> children)
      throws BindException,MarkupException
    { 
      elementContent=ContentUnit.this.content;
      
      
      if (elementContent!=null && ContentUnit.this.getParent().getTrim())
      { elementContent=elementContent.toString().trim();
      }
      
      super.bind(children);
    }
    
    @Override
    public void render(EventContext context)
      throws IOException
    { 
      if (elementContent!=null)
      { context.getWriter().write(elementContent.toString());
      }
    }
  }
}
