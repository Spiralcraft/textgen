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
import spiralcraft.lang.Focus;
import spiralcraft.lang.DefaultFocus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;

import spiralcraft.lang.decorators.IterationDecorator;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.compiler.TglUnit;

import java.io.Writer;

import java.io.IOException;
import java.util.List;

/**
 * Iterate through an Iterable or an Array
 */
public class Iterate
  extends Element
{
  
  private Expression<?> expression;
  private Focus focus;
  private IterationDecorator iterator;
  
  public void setX(Expression expression)
  { this.expression=expression;
  }
  
  public Focus getFocus()
  { return focus;
  }
  

  @SuppressWarnings("unchecked") // Not using generic versions
  public void bind(Element parent,List<TglUnit> childUnits)
    throws BindException
  { 
    Focus<?> parentFocus=parent.getFocus();
    Optic<?> target=null;
    if (expression!=null)
    { target=parentFocus.bind(expression);
    }
    else
    { target=parentFocus.getSubject();
    }
    
    iterator=
      target.<IterationDecorator>decorate(IterationDecorator.class);
    
    if (iterator==null)
    { 
      throw new BindException
        ("Cannot iterate through a "+target.getContentType().getName());
    }
    
    focus=(new DefaultFocus(iterator));
    
    bindChildren(childUnits);
  }
  
  public void write(Writer out)
    throws IOException
  { 
    iterator.reset();
    while (iterator.hasNext())
    { 
      iterator.next();
      writeChildren(out);
    }
  }
}
