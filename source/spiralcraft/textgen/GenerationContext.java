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

import java.io.Writer;

/**
 * Provides a mechanism for Elements to share state within a single generation
 *   run.
 * 
 * @author mike
 */
public class GenerationContext
{
  private final GenerationContext parent;
  private Writer writer;
  
  /**
   * Create a GenerationContext that does not refer to any ancestors, and sends
   *   output to the specified Writer.
   */
  public GenerationContext(Writer writer)
  { 
    this.parent=null;
    this.writer=writer;
  }
  
  /**
   * Create a GenerationContext that refers to its ancestors for the resolution
   *   of dependencies.
   * 
   * @param parent The parent GenerationContext
   */
  public GenerationContext(GenerationContext parent)
  { 
    this.parent=parent;
    this.writer=this.parent.getWriter();
  }
  
  public Writer getWriter()
  { return writer;
  }
  
  public void setWriter(Writer writer)
  { this.writer=writer;
  }
  
  
}
