//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.textgen.kit;

import java.util.LinkedList;
import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.lang.ChainableContext;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.textgen.Element;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.common.ContextualException;

/**
 * <p>An Element designed for better extensibility, and for migration to
 *   the spiralcraft.app.Component model.
 * </p>
 * 
 * <p>In this implementation, bind(), message() and render() are declared
 *   final. render() is implemented by delegating a RenderMessage to all
 *   event handlers.
 * </p>
 * @author mike
 *
 */
public class StandardElement
  extends Element
{
  
  private boolean bound;
  protected ChainableContext outerContext;
  
  private LinkedList<Contextual> parentContextuals;
  private LinkedList<Contextual> exportContextuals;
  private LinkedList<Contextual> selfContextuals;  

  protected Focus<?> selfFocus;
  protected boolean exportSelf;
  
  
  @Override
  public final Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  { 
    bound=true;

    bindContextuals(focusChain,parentContextuals);
    
    Contextual self=new Contextual()
    {
      @Override
      public Focus<?> bind(Focus<?> focusChain)
        throws ContextualException
      { return bindInternal(focusChain);
      }
    };
    
    if (outerContext!=null)
    { 
      outerContext.seal(self);
      return outerContext.bind(focusChain);
    }
    else
    { return self.bind(focusChain);
    }
    
  }
  
  public final boolean isBound()
  { return bound;
  }
  
  /**
   * <p>Add a Contextual to be bound to this Control's parent's context.
   * </p>
   * 
   * <p>The Focus returned by the Contextual will not be used by this
   *   component.
   * </p>
   * 
   * @param contextual
   */
  protected void addParentContextual(Contextual contextual)
  { 
    if (this.parentContextuals==null)
    { this.parentContextuals=new LinkedList<Contextual>();
    }
    this.parentContextuals.add(contextual);
  }

  /**
   * <p>Remove a Contextual from the list of Contextuals to be bound
   * </p>
   * 
   * @param contextual
   */
  protected void removeParentContextual(Contextual contextual)
  {
    if (this.parentContextuals!=null)
    { this.parentContextuals.remove(contextual);
    }
  }
  
  /**
   * <p>Add a Contextual to be bound to this Control's target's context 
   * </p>
   * 
   * <p>The Focus returned by the Contextual will not be used by this
   *   component.
   * </p>
   *
   * @param contextual
   */
  protected void addExportContextual(Contextual contextual)
  { 
    if (this.exportContextuals==null)
    { this.exportContextuals=new LinkedList<Contextual>();
    }
    this.exportContextuals.add(contextual);
  }

  /**
   * <p>Remove a Contextual from the list of Contextuals to be bound
   * </p>
   * 
   * @param contextual
   */
  protected void removeExportContextual(Contextual contextual)
  {
    if (this.exportContextuals!=null)
    { this.exportContextuals.remove(contextual);
    }
  }
  
  /**
   * <p>Add a Contextual to be bound to this Control's own context 
   * </p>
   * 
   * <p>The Focus returned by the Contextual will not be used by this
   *   component.
   * </p>
   * 
   * @param contextual
   */
  protected void addSelfContextual(Contextual contextual)
  { 
    if (this.selfContextuals==null)
    { this.selfContextuals=new LinkedList<Contextual>();
    }
    this.selfContextuals.add(contextual);
  }
  
  
  /**
   * </p>Remove a Contextual from the list of Contextuals to be bound
   * <p>
   * 
   * @param contextual
   */
  protected void removeSelfContextual(Contextual contextual)
  {
    if (this.selfContextuals!=null)
    { this.selfContextuals.remove(contextual);
    }
  }
  
  
  protected final void bindContextuals
    (Focus<?> focus,List<Contextual> contextuals)
    throws ContextualException
  { 
    if (contextuals!=null)
    {
      for (Contextual contextual:contextuals)
      { contextual.bind(focus);
      }
    }
  }  
  
  protected Focus<?> bindImports(Focus<?> focusChain)
      throws BindException
  { return focusChain;
  }

  protected Focus<?> bindExports(Focus<?> focusChain)
    throws BindException
  { return focusChain;
  }
  
  protected void bindComplete(Focus<?> focusChain)
    throws BindException
  { }
  
  private final Focus<?> bindInternal(Focus<?> focusChain)
    throws ContextualException
  {
      
    Focus<?> context=focusChain;
    if (selfFocus==null)
    { 
      selfFocus=focusChain.chain
        (new SimpleChannel<StandardElement>(StandardElement.this,true));
      bindContextuals(selfFocus,selfContextuals);
    }

    focusChain=bindImports(focusChain);
    
    bindHandlers(focusChain);
//    focusChain=handlers.bind(focusChain);
      
    focusChain=bindExports(focusChain);
    if (exportSelf)
    {
      if (focusChain==context)
      { focusChain=selfFocus;
      }
      else
      { focusChain.addFacet(selfFocus);
      }
    }
    bindContextuals(focusChain,exportContextuals);

    try
    { bindChildren(focusChain);
    }
    catch (MarkupException x)
    { throw new BindException("Error binding children ",x);
    }
    
    bindComplete(focusChain);
    // XXX We shouldn't return anything that relies on a Context. Ideally,
    //   we should return selfFocus only.
    return focusChain;
  }  
  
  @Override
  public final void message
    (Dispatcher context
    ,Message message
    )
  {    
    if (outerContext!=null)
    { outerContext.push();
    }
  
    try
    { super.message(context,message);
    }
    finally
    { 
      if (outerContext!=null)
      { outerContext.pop();
      }
    }
  }


}
