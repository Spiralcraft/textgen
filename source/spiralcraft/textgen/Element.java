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
package spiralcraft.textgen;

import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

import java.io.Writer;
import java.io.IOException;

/**
 * A stateful processing unit in a TGL block
 *   associated with an ElementUnit node in the
 *   document structure.
 *
 * A tree of Elements is generated each time the Unit tree
 *   in the document is bound to an application context.
 */
public abstract class Element
{ 
  private Element[] _children;
  private Element _parent;
  
  /**
   * Called when binding Units
   */
  public void setChildren(Element[] children)
  { _children=children;
  }

  public Element[] getChildren()
  { return _children;
  }
  
  public Focus getFocus()
  { return _parent.getFocus();
  }
  
  /**
   * Called when binding Units. When this method is called,
   *   a Element's ancestors will be visible but its children
   *   will not be.
   */
  public void bind(Element parent)
    throws BindException
  { _parent=parent;
  }

  protected void writeChildren(Writer writer)
    throws IOException
  {
    if (_children!=null)
    { 
      for (int i=0;i<_children.length;i++)
      { _children[i].write(writer);
      }
    }
  }

  /**
   * Recursively perform processing and write output
   */
  public abstract void write(Writer writer)
    throws IOException;
}
