package spiralcraft.textgen;



import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;

/**
 * <p>A FocusElement that publishes data sourced from an expression. 
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class ExpressionFocusElement<T>
    extends FocusElement<T>
{
  private Expression<T> expression;
  private Channel<T> source;
  
  public void setX(Expression<T> expression)
  { this.expression=expression;
  }
  
  @Override
  @SuppressWarnings("unchecked") // Not using generic versions
  public Channel<T> bindSource(Focus<?> focusChain)
    throws BindException
  { 
    
    if (expression!=null)
    { source=focusChain.bind(expression);
    }
    else
    { source=(Channel<T>) focusChain.getSubject();
    }
    return source;
  }

  /**
   * 
   * @return The source channel for the data value that will be retrieved
   *   and cached in this element. 
   */
  protected Channel<T> getSource()
  { return source;
  }
  
  @Override
  protected Focus<?> bindExports(Focus<?> focusChain) throws BindException
  { return focusChain;
  }

  /**
   * Called when the Stateful value should be recomputed
   * 
   */
  @Override  
  protected T compute()
  { 
    T val=source.get();
    if (debug)
    { 
      log.fine
        ("Recomputing "+(expression!=null?expression:"")+" ("+source+") = "
        +val);
    }
    return val;
  }
}
