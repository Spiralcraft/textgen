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
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.log.ClassLog;

import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.Element;

import spiralcraft.textgen.compiler.TglUnit;

import spiralcraft.text.markup.MarkupException;

import java.io.IOException;

import java.util.List;

/**
 * <P>Provides a means for determining at which points the sort key or 
 *   a portion of the sort key changes when Iterating over a list of 
 *   items grouped by the sort key. 
 * </P>
 * 
 * @author mike
 *
 */
public class Group
  extends Element
{
  private static final ClassLog log=ClassLog.getInstance(Group.class);

  private Expression<?> expression;
  private Channel<?> current;
  private Channel<?> lookahead;
  private Channel<?> lookbehind;
  
  private Iterate iterate;
  private Group parentGroup;

  private Focus<?> focus;
  

  
  
  public void setX(Expression<?> expression)
  { this.expression=expression;
  }
  
  @Override
  @SuppressWarnings("unchecked") // Not using generic versions
  public void bind(List<TglUnit> childUnits)
    throws BindException,MarkupException
  { 
    if (expression==null)
    {
      throw new BindException
        ("Group element must be assigned an expression in the 'x' property ");
      
    }
    
    iterate=findElement(Iterate.class);
    if (iterate==null)
    { 
      throw new BindException
        ("Group element must be contained in Iterate element");
    }    
    
    parentGroup=getParent().findElement(Group.class,Iterate.class);
    
    Focus parentFocus=getParent().getFocus();
    
    current=iterate.getFocus().bind(expression);
    lookahead=iterate.getLookaheadFocus().bind(expression);    
    lookbehind=iterate.getLookbehindFocus().bind(expression);    
    
    SimpleFocus<?> focus
      =new SimpleFocus(parentFocus,parentFocus.getSubject());
    focus.addFacet(getAssembly().getFocus());
    this.focus=focus;
    
    
    bindChildren(childUnits);
  }
  
  @Override
  public Focus<?> getFocus()
  { return focus;
  }
  
  
  private boolean changed(Object a,Object b)
  {
    if (debug)
    { log.fine(a+" ? "+b);
    }
    if (a==b)
    { return false;
    }
    else if (a==null || b==null)
    { return true;
    }
    else
    { 
      // TODO: Use comparator for collation sensitivity
      return !a.equals(b);
    }
      
  }
  
  public boolean isStarting()
  { 
    return changed(current.get(),lookbehind.get())
      || (parentGroup!=null && parentGroup.isStarting())
      || iterate.isFirst();
    
  }
  
  public boolean isEnding()
  { 
    return changed(current.get(),lookahead.get())
      || (parentGroup!=null && parentGroup.isEnding())
      || iterate.isLast();
  }
  
  @Override
  public void render(EventContext context)
    throws IOException
  { renderChildren(context);
  }
  
}
