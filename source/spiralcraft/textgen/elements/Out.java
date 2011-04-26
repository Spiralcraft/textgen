package spiralcraft.textgen.elements;

import java.io.IOException;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.kit.RenderHandler;

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
  
  { addHandler(new RenderHandler()
      {
        
        @Override
        protected void render(EventContext dispatcher)
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
          { dispatcher.getOutput().append(value.toString());
          }          
        }
      }
    );
  }

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
    throws ContextualException
  { 
    x.bind(focus);
    return super.bind(focus);
  
  }
  
}
