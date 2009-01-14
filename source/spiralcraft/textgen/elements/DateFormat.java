//
// Copyright (c) 2008,2009 Michael Toth
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.compiler.TglUnit;

/**
 * <P>Creates a new Focus for expression evaluation and additionally holds
 *   the referenced value for repeated access by this element's children
 *   during a render or message cycle.
 * </P>
 * 
 * @author mike
 *
 * @param <T>
 */
public class DateFormat
    extends Element
{
  private Expression<?> expression;
  private Expression<TimeZone> timeZoneExpression;
  
  private Channel<?> target;
  private Channel<TimeZone> timeZone;
  
  private final ThreadLocal<SimpleDateFormat> formatLocal
    =new ThreadLocal<SimpleDateFormat>();
  
  private String formatString;
  
  public void setX(Expression<?> expression)
  { this.expression=expression;
  }
  
  public void setTimeZone(Expression<TimeZone> timeZoneExpression)
  { this.timeZoneExpression=timeZoneExpression;
  }
  
  public void setFormat(String formatString)
  { this.formatString=formatString;
  }
  
  @Override
  public void bind(List<TglUnit> childUnits)
    throws BindException,MarkupException
  { 
    Focus<?> parentFocus=getParent().getFocus();
    
    if (expression!=null)
    { target=parentFocus.bind(expression);
    }
    else
    { target=parentFocus.getSubject();
    }
    
    formatLocal.set(new SimpleDateFormat(formatString));
    
    if (timeZoneExpression!=null)
    { timeZone=parentFocus.bind(timeZoneExpression);
    }
    
    super.bind(childUnits);
  }
  

  @Override
  public void render(EventContext context)
    throws IOException
  { 
    Object val=target.get();
    if (val!=null)
    {
      SimpleDateFormat format=formatLocal.get();
      if (format==null)
      { 
        format=new SimpleDateFormat(formatString);
        formatLocal.set(format);
      }
      if (timeZone!=null)
      { 
        TimeZone zone=timeZone.get();
        // XXX Always re-apply default time zone if null- potential state leak
        if (zone!=null)
        { format.setTimeZone(zone);
        }
        
        
      }
      context.getWriter().write(format.format(val));
    }
  }
}
