package spiralcraft.textgen.compiler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.common.Lifecycle;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusChainObject;
import spiralcraft.lang.ThreadedFocusChainObject;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.Message;
import spiralcraft.util.thread.Delegate;
import spiralcraft.util.thread.DelegateException;

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
  private final FocusChainObject fco;
  private final ThreadedFocusChainObject tfco;
  private Focus<?> focus;
  
  public FocusChainElement(Object object)
  { 
    this.object=object;
    if (object instanceof FocusChainObject)
    { fco=(FocusChainObject) object;
    }
    else
    { fco=null;
    }
    
    if (object instanceof ThreadedFocusChainObject)
    { tfco=(ThreadedFocusChainObject) object;
    }
    else
    { tfco=null;
    }
  }
  
  public Object getObject()
  { return object;
  }

  @Override
  public void bind(List<TglUnit> childUnits) 
    throws MarkupException, BindException 
  { 
    focus=getParent().getFocus();
    
    if (fco!=null)
    { focus=fco.bind(focus);
    }
    else
    { focus=focus.chain(getAssembly().getFocus().getSubject());
    }
    super.bind(childUnits);
    
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
      try
      { 
        tfco.runInContext
          (new Delegate<Void>()
          {
            @Override
            public Void run()
              throws DelegateException
            { 
              FocusChainElement.super.message(context,message,path);
              return null;
            }
          }
          );
      }
      catch (DelegateException x)
      { throw new RuntimeException(x);
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
     try
       { 
        tfco.runInContext
          (new Delegate<Void>()
          {
            @Override
            public Void run()
              throws DelegateException
            { 
              try
              { 
                renderChildren(context);
                return null;
              }
              catch (IOException x)
              { throw new DelegateException(x);
              }
            }
          }
          );
      }
      catch (DelegateException x)
      { 
        if (x.getCause() instanceof IOException)
        { throw (IOException) x.getCause();
        }
        else
        { throw new RuntimeException(x);
        }
      }
    }
  }

}
