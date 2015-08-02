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
import spiralcraft.util.string.StringPool;
import spiralcraft.util.string.StringUtil;


/**
 * A Unit which contains literal text
 */
public class ContentUnit
  extends TglUnit
{
  private String content;
  
  public ContentUnit(TglUnit parent,CharSequence content,TglCompiler<?> compiler)
  { 
    super(parent,compiler);
    
    this.content=StringPool.INSTANCE.get(content.toString());
  }
  
  @Override
  public Element createElement()
  { 
    String elementContent=content;
      
      
    if (elementContent!=null && Boolean.TRUE.equals(trim))
    { elementContent=StringPool.INSTANCE.get(elementContent.trim());
    }
      
    return new TextElement(elementContent);
  }
  
  @Override
  public CharSequence getContent()
  { return content;
  }
  
  public void trimStart()
  { 
    content=StringPool.INSTANCE.get(StringUtil.trimStart(content.toString()));
    // log.fine("Trim start ["+content+"]");
  }
  
  public void trimEnd()
  { 
    content=StringPool.INSTANCE.get(StringUtil.trimEnd(content.toString()));
    // log.fine("Trim end ["+content+"]");
  }
  

  
  @Override
  public String toString()
  { return super.toString()+": "+content;
  }
  
}

