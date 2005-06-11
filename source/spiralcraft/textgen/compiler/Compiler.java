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

import spiralcraft.text.Trimmer;

import spiralcraft.text.markup.MarkupCompiler;
import spiralcraft.text.markup.CompilationUnit;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.SyntaxException;

/**
 * Compiles a CharSequence containing Text Generation Markup Language
 *   into a tree of Units that can later be bound to an application
 *   context.
 */
public class Compiler
  extends MarkupCompiler
{
  
  private final Trimmer _trimmer=new Trimmer("\r\n\t ");
  
  public Compiler()                
  { super("<%","%>");
  }

  public CompilationUnit createCompilationUnit()
  { return new TglCompilationUnit();
  }
  
  public void handleMarkup(CharSequence code)
    throws Exception
  { 
    code=_trimmer.trim(code);
    if (code.charAt(0)=='/')
    { 
      // End tag case
      String unitName=code.subSequence(1,code.length()).toString();
      String expectName=getUnit().getName();
      if (unitName.equals(expectName))
      { closeUnit();
      }
      else
      { 
        if (expectName!=null)
        { 
          throw new SyntaxException
            ("Mismatched end tag. Found <%/"+unitName+"%>"
            +", expecting <%/"+expectName+"%>"
            );
        }
        else
        {
          throw new SyntaxException
            ("Unexpected end tag <%/"+unitName+"%>- no tags are open");
        }
      }
    }
    else
    {
      ElementUnit elementUnit=new ElementUnit(code);
      addUnit(elementUnit);
    }
  }
}
