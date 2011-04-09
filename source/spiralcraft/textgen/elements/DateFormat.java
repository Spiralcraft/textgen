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

import java.text.SimpleDateFormat;

import java.util.TimeZone;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.text.markup.MarkupException;

/**
 * <p>Formats a date
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class DateFormat
  extends Format<SimpleDateFormat>
{
  private Expression<TimeZone> timeZoneExpression;
  
  private Channel<TimeZone> timeZone;
  
  
  private String formatString;
  
  
  public void setTimeZone(Expression<TimeZone> timeZoneExpression)
  { this.timeZoneExpression=timeZoneExpression;
  }
  
  public void setFormat(String formatString)
  { this.formatString=formatString;
  }
  
  @Override
  protected SimpleDateFormat createFormat()
  { return new SimpleDateFormat(formatString);
  }
  
  @Override
  protected void updateFormat(SimpleDateFormat format)
  { 
    if (timeZone!=null)
    { 
      TimeZone zone=timeZone.get();
      // XXX Always re-apply default time zone if null- potential state leak
      if (zone!=null)
      { format.setTimeZone(zone);
      }
    }
  }
  
  @Override
  public Focus<?> bind(Focus<?> parentFocus)
    throws BindException,MarkupException
  { 
    if (timeZoneExpression!=null)
    { timeZone=parentFocus.bind(timeZoneExpression);
    }
    
    return super.bind(parentFocus);
  }
}
