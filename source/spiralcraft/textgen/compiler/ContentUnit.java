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

import spiralcraft.app.Dispatcher;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.OutputContext;
import spiralcraft.textgen.kit.RenderHandler;
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
  { return new TextElement();
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
  
  class TextElement
    extends Element
  {
    private String elementContent;
    
    { addHandler
        (new RenderHandler() 
          {
            @Override
            protected void render(Dispatcher context)
              throws IOException
            { 
              if (elementContent!=null)
              { OutputContext.get().append(elementContent.toString());
              }              
            }
          } 
        );
    }
    
    @Override
    protected Focus<?> bindStandard(Focus<?> focus)
      throws ContextualException
    { 
      elementContent=ContentUnit.this.content;
      
      
      if (elementContent!=null && Boolean.TRUE.equals(trim))
      { elementContent=StringPool.INSTANCE.get(elementContent.trim());
      }
      
      return super.bindStandard(focus);
    }
    
  }
  
  @Override
  public String toString()
  { return super.toString()+": "+content;
  }
  
}
