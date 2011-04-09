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

import spiralcraft.text.markup.MarkupException;

import spiralcraft.textgen.Element;

import spiralcraft.text.xml.Attribute;

/**
 * A Unit which discards its contents
 */
public class CommentUnit
  extends ProcessingUnit
{
  
  private boolean shorthand;
  
  public CommentUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,Attribute[] attribs
    )
    throws MarkupException
  { 
    super(parent,compiler.getPosition());
    
    if (attribs!=null && attribs.length>0)
    {
      throw new MarkupException
        ("@comment does not accept attributes"
        ,compiler.getPosition()
        );
    }
    
  }
  
  public CommentUnit
  (TglUnit parent
  ,TglCompiler<?> compiler
  )
  throws MarkupException
  { 
    super(parent,compiler.getPosition());
    shorthand=true;
  }

  
  @Override
  public String getName()
  { return shorthand?"!":"@comment";
  }
  
  @Override
  public Element createElement()
  { return new NullElement();
  }
  
  
}

