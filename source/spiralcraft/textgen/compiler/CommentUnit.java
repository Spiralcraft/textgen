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

import spiralcraft.lang.Focus;
import spiralcraft.text.markup.MarkupException;

import spiralcraft.textgen.Element;

import spiralcraft.text.xml.Attribute;

/**
 * A Unit which discards its contents
 */
public class CommentUnit
  extends ProcessingUnit
{
  
  
  public CommentUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,Attribute[] attribs
    )
    throws MarkupException
  { 
    super(parent);
    
    if (attribs!=null && attribs.length>0)
    {
      throw new MarkupException
        ("@comment does not accept attributes"
        ,compiler.getPosition()
        );
    }
    
  }
  
  
  @Override
  public String getName()
  { return "@comment";
  }
  
  @Override
  public Element bind(Focus<?> focus,Element parentElement)
    throws MarkupException
  { return new NullElement(parentElement);
  }
  
  
}

