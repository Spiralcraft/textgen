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

import spiralcraft.textgen.Element;
import spiralcraft.textgen.GenerationContext;


/**
 * A Unit which contains literal text
 */
public class TglContentUnit
  extends TglUnit
{
  private final CharSequence content;
  
  public TglContentUnit(CharSequence content)
  { this.content=content;
  }
  
  public Element bind(Element parentElement)
  { return new TextElement();
  }
  
  class TextElement
    extends Element
  {
    public void write(GenerationContext context)
      throws IOException
    { context.getWriter().write(content.toString());
    }
  }
}
