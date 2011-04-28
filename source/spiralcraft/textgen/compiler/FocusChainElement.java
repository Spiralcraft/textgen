package spiralcraft.textgen.compiler;


import spiralcraft.common.ContextualException;
import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Context;
import spiralcraft.textgen.Element;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;

/**
 * <p>Puts an object reference into the Focus chain
 * </p>
 * 
 * @author mike
 *
 */
public class FocusChainElement
    extends Element
{

  private final Object object;
  private final Contextual fco;
  private final Context tfco;
  
  public FocusChainElement(Object object)
  { 
    this.object=object;
    if (object instanceof Contextual)
    { fco=(Contextual) object;
    }
    else
    { fco=null;
    }
    
    if (object instanceof Context)
    { tfco=(Context) object;
    }
    else
    { tfco=null;
    }
  }
  
  public Object getObject()
  { return object;
  }
  


  @Override
  public Focus<?> bind(Focus<?> context) 
    throws ContextualException 
  { 
    
    Focus<?> focus;
    if (fco!=null)
    { focus=fco.bind(context);
    }
    else
    { focus=context.chain(getAssembly().getFocus().getSubject());
    }
    focus.addFacet(getAssembly().getFocus());
    super.bind(focus);
    
    if (object instanceof Lifecycle)
    { 
      try
      { ((Lifecycle) object).start();
      }
      catch (LifecycleException x)
      { throw new BindException("Error starting object "+object,x);
      }
    }
    return focus;
  }
  
  @Override
  public void message
    (final Dispatcher context
    ,final Message message
    )
  { 
    
    if (tfco==null)
    { super.message(context,message);
    }
    else
    {
      tfco.push();
      try
      { super.message(context,message);
      }
      finally
      { tfco.pop();
      }
    }
    
    
  }

}
