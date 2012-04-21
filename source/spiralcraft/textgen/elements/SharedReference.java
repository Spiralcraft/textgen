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

import java.net.URI;

import spiralcraft.common.ContextualException;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.persist.AbstractXmlObject;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.textgen.Element;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;

/**
 * <p>Exposes an object reference via the Focus chain, via the
 *   spiralcraft.data.persist mechanism. The lifecycle of the object
 *   is scoped to the bound resource, and the object is shared among
 *   all individual state trees and renderings.
 * </p>
 * 
 * <p>If the referent implements spiralcraft.lang.Contextual,
 *   the referent will be bound into the Focus chain so it can publish
 *   any necessary interfaces.
 * </p>
 * 
 * <p>If the same Element tree is used by multiple threads, the referent
 *   should synchronize its own state.
 * </p>
 * 
 * 
 * @author mike
 *
 * @param <Treferent>
 */
public class SharedReference<Treferent>
    extends Element
{
  private Type<?> type;
  private Expression<Type<?>> typeX;
  private URI instanceURI;
  private AbstractXmlObject<Treferent,?> reference;
  
  
  @Override
  protected Focus<?> bindStandard(Focus<?> focusChain)
    throws ContextualException
  { 
    if (type==null && typeX!=null)
    { type=focusChain.bind(typeX).get();
    }
    
    focusChain=focusChain.chain(getAssembly().getFocus().getSubject());
    
    
    reference
      =AbstractXmlObject.<Treferent>activate
        (type!=null?type.getURI():null,instanceURI,focusChain);

    
    return super.bindStandard(reference.getFocus());
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
  protected void messageStandard
    (final Dispatcher context
    ,final Message message
    )
  { 
    reference.push();
    try
    { super.messageStandard(context,message);
    }
    finally
    { reference.pop();
    }
  }
  
}



