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

import spiralcraft.textgen.Element;
import spiralcraft.textgen.RenderingContext;

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
  { return new DefaultElement();
  }
  
  
}

class DefaultElement
  extends Element
{
  public void write(RenderingContext context)
    throws IOException
  { writeChildren(context);
  }
}
