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
import spiralcraft.text.markup.MarkupException;

import spiralcraft.text.ParseException;


/**
 * <P>Compiles a CharSequence containing Text Generation Markup Language
 *   into a tree of Units that can later be bound to an application
 *   context.
 * 
 * <P>Each unit in the tree is either a TglContentUnit, which contains literal
 *   text, or a TglElementUnit, which represents a functionality specified by 
 *   a markup element.
 *   
 * <P>The MarkupCompiler superclass provides the basic mechanism to parse 
 *   an abstract template-style markup language. The TglCompiler further
 *   specifies this mechanism by introducing specific standard delimiters
 *   (&lt;% %&gt;) as well as the concept of open and closed tags.
 */
public class TglCompiler
  extends MarkupCompiler<TglUnit>
{
  
  private final Trimmer _trimmer=new Trimmer("\r\n\t ");
  
  public TglCompiler()                
  { 
    super("<%","%>");
  }

  
  public void handleContent(CharSequence content)
  { pushUnit(new TglContentUnit(content));
  }
  
  public void handleMarkup(CharSequence code)
    throws MarkupException,ParseException
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
          throw new MarkupException
            ("Mismatched end tag. Found <%/"+unitName+"%>"
            +", expecting <%/"+expectName+"%>"
            ,position
            );
        }
        else
        {
          throw new MarkupException
            ("Unexpected end tag <%/"+unitName+"%>- no tags are open",position);
        }
      }
    }
    else
    {
      TglElementUnit tglElementUnit=new TglElementUnit(code,position);
      pushUnit(tglElementUnit);
    }
  }
}
