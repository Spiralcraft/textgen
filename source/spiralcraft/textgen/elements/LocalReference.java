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
import spiralcraft.lang.BindException;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Context;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.ElementState;
import spiralcraft.textgen.EventContext;

import spiralcraft.app.Message;

/**
 * <p>Exposes an object reference via the Focus chain, via the
 *   spiralcraft.data.persist mechanism. The lifecycle of the object
 *   is scoped to the local state, unless the "stateless" property
 *   is set or a stateless rendering is used, in which case the object
 *   will be re-created for every pass through render() or message().
 *    
 * </p>
 * 
 * <p>Note that if the referent implements spiralcraft.lang.Contextual,
 *   it will have access to the Focus Chain, but it cannot publish its own
 *   interfaces into the chain due to its narrower lifecycle.
 * </p>
 * 
 * @author mike
 *
 * @param <Treferent>
 */
public class LocalReference<Treferent>
    extends Element
{
  private ThreadLocalChannel<Treferent> channel;
  private Focus<Treferent> focus;
  private Type<?> type;
  private URI instanceURI;
  private boolean stateless;
  private Focus<?> context;
  

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
  @SuppressWarnings({"unchecked","rawtypes"}) // Not using generic versions
  public Focus<?> bind(Focus<?> parentFocus)
    throws ContextualException
  { 
    
    if (type==null)
    { 
      throw new BindException
        ("TypeURI must be specified");
    }
    this.context=parentFocus;
    
    // This instance just used to infer a type
    AbstractXmlObject ref
      =AbstractXmlObject.activate
        (type.getURI(),instanceURI,parentFocus);
        
    channel=new ThreadLocalChannel
      (BeanReflector.getInstance(ref.get().getClass()));
    
    if (Context.class.isAssignableFrom(ref.get().getClass()))
    { 
      throw new BindException
        ("To properly use this ThreadContextual, use the "
        +" SharedReference element instead of LocalReference"
        );
    }
      
    focus=new SimpleFocus(parentFocus,channel);
    focus.addFacet(getAssembly().getFocus());
    
    return super.bind(focus);
  }

  


  @SuppressWarnings("unchecked")
  @Override
  public void message
    (EventContext context
    ,Message message
    )
  {
    ReferenceState<Treferent> state
      =(ReferenceState<Treferent>) context.getState();
    
    try
    { 
      AbstractXmlObject<Treferent,?> reference;
      if (state!=null && !stateless)
      { reference=state.getReference(type.getURI(),instanceURI);
      }
      else
      { 
        reference
          =AbstractXmlObject.<Treferent>activate
            (type.getURI()
            ,instanceURI
            ,this.context
            );
      }
      
      channel.push(reference.get());
    }
    catch (ContextualException x)
    { throw new RuntimeException("Error creating XML Object reference",x);
    }
    
    
    try
    { super.message(context,message);
    }
    finally
    { channel.pop();
    }
  }

  
  @Override
  public ReferenceState<Treferent> createState()
  { return new ReferenceState<Treferent>(this.getChildCount());
  }
}

class ReferenceState<T>
  extends ElementState
{
  private AbstractXmlObject<T,?> reference;
  
  public ReferenceState(int childCount)
  { super(childCount);
  }
  
  public synchronized AbstractXmlObject<T,?> getReference
    (URI typeURI,URI instanceURI)
    throws ContextualException
  { 
    if (reference==null)
    { 
      reference
        =AbstractXmlObject.<T>activate(typeURI,instanceURI,null);
    }

    return reference;
  }
}

