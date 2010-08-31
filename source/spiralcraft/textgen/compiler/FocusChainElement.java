package spiralcraft.textgen.compiler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.ThreadContextual;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.Message;

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
  private final ThreadContextual tfco;
  
  public FocusChainElement(Object object)
  { 
    this.object=object;
    if (object instanceof Contextual)
    { fco=(Contextual) object;
    }
    else
    { fco=null;
    }
    
    if (object instanceof ThreadContextual)
    { tfco=(ThreadContextual) object;
    }
    else
    { tfco=null;
    }
  }
  
  public Object getObject()
  { return object;
  }
  


  @Override
  public void bind(Focus<?> focus,List<TglUnit> childUnits) 
    throws MarkupException, BindException 
  { 
    
    
    if (fco!=null)
    { focus=fco.bind(focus);
    }
    else
    { focus=focus.chain(getAssembly().getFocus().getSubject());
    }
    focus.addFacet(getAssembly().getFocus());
    super.bind(focus,childUnits);
    
    if (object instanceof Lifecycle)
    { 
      try
      { ((Lifecycle) object).start();
      }
      catch (LifecycleException x)
      { throw new BindException("Error starting object "+object,x);
      }
    }
  }
  
  @Override
  public void message
    (final EventContext context
    ,final Message message
    ,final LinkedList<Integer> path
    )
  { 
    
    if (tfco==null)
    { super.message(context,message,path);
    }
    else
    {
      tfco.push();
      try
      { super.message(context,message,path);
      }
      finally
      { tfco.pop();
      }
    }
    
    
  }
  
  @Override
  public void render(final EventContext context)
    throws IOException
  { 
    if (tfco==null)
    { renderChildren(context);
    }
    else
    {
      tfco.push();
      try
      { renderChildren(context);
      }
      finally
      { tfco.pop();
      }
    }
  }

}
