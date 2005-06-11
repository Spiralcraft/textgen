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

import java.io.Writer;
import java.io.IOException;

import spiralcraft.textgen.Element;

import spiralcraft.builder.Assembly;

import spiralcraft.text.markup.ContentUnit;

/**
 * A Unit which contains literal text
 */
public class TglContentUnit
  extends ContentUnit
  implements TglUnit
{
  
  public TglContentUnit(CharSequence content)
  { super(content);
  }
  
  public Element bind(Assembly parent,Element parentElement)
  { return new TextElement();
  }
  
  class TextElement
    extends Element
  {
    public void write(Writer writer)
      throws IOException
    { 
      CharSequence content=getContent();
      int len=content.length();
      for (int i=0;i<len;i++)
      { writer.write(content.charAt(i));
      }
    }
  }
}
