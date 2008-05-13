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

import java.io.PrintWriter;

import spiralcraft.lang.BindException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;


import spiralcraft.text.ParseException;
import spiralcraft.text.markup.Unit;
import spiralcraft.text.markup.MarkupException;

import java.io.IOException;

/**
 * A Unit of text generation which represents a
 *   node in the tree structure of a TGL block.
 */
public abstract class TglUnit
  extends Unit<TglUnit>
{
  
  protected boolean allowsChildren=true;
  protected boolean trim;
  
  public TglUnit(TglUnit parent)
  { super(parent);
  }
  
  public boolean allowsChildren()
  { return allowsChildren;
  }
    
  /**
   * <P>Create a tree of Elements bound into an application context
   *   (the Assembly) which implements the functional behavior 
   *   specified by the TGL document.
   */
  public abstract Element bind(Element parentElement)
    throws MarkupException;

  public void dumpTree(PrintWriter writer,String linePrefix)
  { 
    writer.println(linePrefix+toString());
    for (TglUnit unit: children)
    { unit.dumpTree(writer,linePrefix+"  ");
    }
  }  
  
  protected Element defaultBind(Element parentElement)
    throws MarkupException
  { 
    Element element=new DefaultElement();
    element.setParent(parentElement);
    try
    { element.bind(children);
    }
    catch (BindException x)
    { throw new MarkupException(x.toString(),getPosition(),x);
    }
    return element;
  }
  
  protected void addUnitAttribute(String name,String value)
    throws ParseException
  { 
    name=name.intern();
    if (name=="trim")
    { trim=Boolean.parseBoolean(value);
    }
    else
    { 
      throw new ParseException
        ("Unrecognized textgen attribute '"+name
        +"' - not one of {trim}"
        ,getPosition()
        );
    }
    
  }
  
  /**
   * Whether whitespace should be trimmed from any content blocks directly
   *   contained in this Unit.
   * 
   * @return
   */
  public boolean getTrim()
  { return trim;
  }
  
}

class DefaultElement
  extends Element
{
  public void render(EventContext context)
    throws IOException
  { renderChildren(context);
  }
}
