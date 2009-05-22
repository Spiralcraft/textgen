//
// Copyright (c) 1998,2008 Michael Toth
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

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.persist.AbstractXmlObject;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.Message;
import spiralcraft.textgen.compiler.TglUnit;
import spiralcraft.util.thread.Delegate;
import spiralcraft.util.thread.DelegateException;

/**
 * <p>Exposes an object reference via the Focus chain, via the
 *   spiralcraft.data.persist mechanism. The lifecycle of the object
 *   is scoped to the bound resource, and the object is shared among
 *   all individual state trees and renderings.
 * </p>
 * 
 * <p>If the referent implements spiralcraft.lang.FocusChainObject,
 *   the referent will be bound into the Focus Chain so it can publish
 *   any necessary interfaces.
 * </p>
 * 
 * <p>If the same Element tree is used by multiple threads, the referent
 *   should synchronize its own state.
 * </p>
 * 
 * <p>We use a ThreadDelegate mechanism for the following reason: 
 *   The internal use of a ThreadLocal by the referent for publishing
 *   an object into the Focus chain may result in the object never being
 *   removed from memory. The FocusChainObject needs a set of methods to
 *   allow for push-pop. (The ThreadDelage mechanism accomplishes this).
 * </p>
 * 
 * @author mike
 *
 * @param <Treferent>
 */
public class SharedReference<Treferent>
    extends Element
{
  private Focus<?> focus;
  private Type<?> type;
  private Expression<Type<?>> typeX;
  private URI instanceURI;
  private AbstractXmlObject<Treferent,?> reference;
  
  
  @Override
  public void bind(List<TglUnit> childUnits)
    throws BindException,MarkupException
  { 
    Focus<?> focusChain=getParent().getFocus();
    if (type==null && typeX!=null)
    { type=focusChain.bind(typeX).get();
    }
    
    focusChain=focusChain.chain(getAssembly().getFocus().getSubject());
    
    
    reference
      =AbstractXmlObject.<Treferent>activate
        (type!=null?type.getURI():null,instanceURI,null,focusChain);

    focus=reference.getFocus();
    
    super.bind(childUnits);
  }

  /**
   * <p>An expression which resolves to a spiralcraft.data.Type that
   *   corresponds to the object under consideration
   * 
   * </p>
   * 
   * @param typeURI
   * @throws DataException
   */
  public void setTypeX(Expression<Type<?>> x)
  { this.typeX=x;
  }
  
  /**
   * <p>The URI of the spiralcraft.data.Type that corresponds to the object
   *   must be specified. 
   * </p>
   * 
   * @param typeURI
   * @throws DataException
   */
  public void setTypeURI(URI typeURI)
    throws DataException
  { type=Type.resolve(typeURI);
  }
  
  /**
   * <p>The URI of the data file, if one exists, which contains the instance 
   *   data for this object can be specified. The data file is in the 
   *   spiralcraft.data native XML format.
   * </p>
   * 
   * @param instanceURI
   * @throws DataException
   */
  public void setInstanceURI(URI instanceURI)
    throws DataException
  { this.instanceURI=instanceURI;
  }

  @Override
  public Focus<?> getFocus()
  { return focus;
  }

  @Override
  public void message
    (final EventContext context
    ,final Message message
    ,final LinkedList<Integer> path
    )
  { 
    try
    { 
      reference.runInContext
        (new Delegate<Void>()
        {
          @Override
          public Void run()
            throws DelegateException
          { 
            SharedReference.super.message(context,message,path);
            return null;
          }
        }
        );
    }
    catch (DelegateException x)
    { throw new RuntimeException(x);
    }
    
    
  }
  
  @Override
  public void render(final EventContext context)
    throws IOException
  { 
    try
    { 
      reference.runInContext
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



