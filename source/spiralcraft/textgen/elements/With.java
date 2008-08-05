package spiralcraft.textgen.elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.Message;
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
public class With<T>
    extends Element
{
  private Expression<T> expression;
  private Channel<T> target;
  private ThreadLocalChannel<T> channel;
  private Focus<T> focus;
  
  public void setX(Expression<T> expression)
  { this.expression=expression;
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
    { target=(Channel<T>) parentFocus.getSubject();
    }
    
    channel=new ThreadLocalChannel(target.getReflector());
    focus=new SimpleFocus(parentFocus,channel);
    
    super.bind(childUnits);
  }
  
  @Override
  public Focus<T> getFocus()
  { return focus;
  }

  @Override
  public void message
    (EventContext context
    ,Message message
    ,LinkedList<Integer> path
    )
  {
    channel.push(target.get());
    
    try
    { super.message(context,message,path);
    }
    finally
    { channel.pop();
    }
  }

  @Override
  public void render(EventContext context)
    throws IOException
  { 
    channel.push(target.get());
    
    try
    { renderChildren(context);
    }
    finally
    { channel.pop();
    }
  }
}
