//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.textgen.elements;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.RenderingContext;

import spiralcraft.textgen.compiler.TglUnit;

import spiralcraft.text.markup.MarkupException;

import java.io.IOException;

import java.util.List;

public class If
  extends Element
{
  private Expression<Boolean> expression;
  private Channel<Boolean> target;
  
  public void setX(Expression<Boolean> expression)
  { this.expression=expression;
  }
  

  @SuppressWarnings("unchecked") // Not using generic versions
  public void bind(Element parent,List<TglUnit> childUnits)
    throws BindException,MarkupException
  { 
    Focus<?> parentFocus=parent.getFocus();
    
    if (expression!=null)
    { target=parentFocus.<Boolean>bind(expression);
    }
    else
    { target=(Channel<Boolean>) parentFocus.getSubject();
    }
    
    if (!Boolean.class.isAssignableFrom(target.getContentType()))
    { throw new BindException
        ("<%If%> requires a boolean expression, not a "+target.getContentType());
    }
    bindChildren(childUnits);
  }
  
  public void write(RenderingContext context)
    throws IOException
  { 
    if (target.get())
    { writeChildren(context);
    }
  }
}
