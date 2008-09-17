package spiralcraft.textgen.elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;
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
  private ThreadLocalChannel<SimpleDateFormat> formatChannel;
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
  @SuppressWarnings("unchecked") // Not using generic versions
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
    
    formatChannel=new ThreadLocalChannel(target.getReflector());
    formatChannel.push(new SimpleDateFormat(formatString));
    
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
      SimpleDateFormat format=formatChannel.get();
      if (timeZone!=null)
      { 
        TimeZone zone=timeZone.get();
        if (zone!=null)
        { format.setTimeZone(zone);
        }
      }
      context.getWriter().write(formatChannel.get().format(val));
    }
  }
}
