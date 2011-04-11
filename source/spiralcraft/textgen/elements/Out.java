package spiralcraft.textgen.elements;

import java.io.IOException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;

/**
 * <p>Output the result of an expression
 * </p>
 * 
 * @author mike
 *
 */
public class Out<T>
  extends Element
{ 
  
  private Binding<T> x;

  /**
   * The expression to output
   * 
   * @param x
   */
  public void setX(Expression<T> x)
  { this.x=new Binding<T>(x);
  }
  
  @Override
  public Focus<?> bind(Focus<?> focus)
    throws BindException,MarkupException
  { 
    x.bind(focus);
    return super.bind(focus);
  
  }
  
  @Override
  public void render(EventContext context)
    throws IOException
  { 
    T value;
    try
    { value=x.get();
    }
    catch (NullPointerException x)
    { 
      x.printStackTrace();
      value=null;
    }
      
    if (value!=null)
    { context.getOutput().append(value.toString());
    }
  }
}
