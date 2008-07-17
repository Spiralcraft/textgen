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

import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.Element;

import spiralcraft.textgen.compiler.TglUnit;

import spiralcraft.text.markup.MarkupException;

import java.io.IOException;

import java.util.List;

/**
 * <P>Logical element displays contents if the condition (X) is true. If the
 *   condition is false, and an "Else" element is a direct child, the contents
 *   following the Else is displayed.
 * </P>
 * 
 * @author mike
 *
 */
public class If
  extends Element
{
  private Expression<Boolean> expression;
  private Channel<Boolean> target;
  private int elsePos=-1;
  
  public void setX(Expression<Boolean> expression)
  { this.expression=expression;
  }
  
  @Override
  @SuppressWarnings("unchecked") // Not using generic versions
  public void bind(List<TglUnit> childUnits)
    throws BindException,MarkupException
  { 
    Focus<?> parentFocus=getParent().getFocus();
    
    if (expression!=null)
    { target=parentFocus.<Boolean>bind(expression);
    }
    else
    { target=(Channel<Boolean>) parentFocus.getSubject();
    }
    
    if (!Boolean.class.isAssignableFrom(target.getContentType())
        && !boolean.class.isAssignableFrom(target.getContentType())
        )
    { throw new BindException
        ("<%If%> requires a boolean expression, not a "+target.getContentType());
    }
    
    bindChildren(childUnits);
    int childCount=getChildCount();
    for (int i=0;i<childCount;i++)
    { 
      if (getChild(i) instanceof Else)
      { 
        elsePos=i;
        break;
      }
    }
  }
  
  /**
   * <P>Renders the tag. A positive result is displayed only if the bound
   *   expression returns true. A null value is interpreted as false.
   * </P>
   */
  public void render(EventContext context)
    throws IOException
  { 
    Boolean val=target.get();
    boolean passed=val!=null && val;
    
    int childCount=getChildCount();

    int start;
    int end;
    if (passed)
    { 
      start=0;
      end=elsePos>-1?elsePos:childCount;
    }
    else
    { 
      start=elsePos>-1?elsePos+1:childCount;
      end=childCount;
    }
    for (int i=start;i<end;i++)
    { renderChild(context,i);
    }
      
  }
}
