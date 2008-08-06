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
import spiralcraft.lang.Channel;
import spiralcraft.lang.CompoundFocus;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.ElementState;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.Message;
import spiralcraft.textgen.compiler.TglUnit;

/**
 * <p>Exposes an object reference via the Focus chain, via the
 *   spiralcraft.data.persist mechanism. The lifecycle of the object
 *   is scoped to the local state, unless the "stateless" property
 *   is set or a stateless rendering is used, in which case the object
 *   will be re-created for every pass through render() or message().
 *    
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
  private CompoundFocus<Treferent> focus;
  private Type<?> type;
  private URI instanceURI;
  private boolean stateless;
  

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
  @SuppressWarnings("unchecked") // Not using generic versions
  public void bind(List<TglUnit> childUnits)
    throws BindException,MarkupException
  { 
    Focus<?> parentFocus=getParent().getFocus();
    
    if (type==null)
    { 
      throw new BindException
        ("TypeURI must be specified");
    }
    
    AbstractXmlObject ref
      =AbstractXmlObject.create(type.getURI(),instanceURI,null);
    
    Channel sample=ref.bind(parentFocus);
    
    channel=new ThreadLocalChannel
      (sample.getReflector());
    
    focus=new CompoundFocus(parentFocus,channel);
    focus.bindFocus(getId(),getAssembly().getFocus());
    
    super.bind(childUnits);
  }

  
  @Override
  public Focus<Treferent> getFocus()
  { return focus;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void message
    (EventContext context
    ,Message message
    ,LinkedList<Integer> path
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
          =AbstractXmlObject.<Treferent>create(type.getURI(),instanceURI,null);
      }
      
      channel.push(reference.get());
    }
    catch (BindException x)
    { throw new RuntimeException("Error creating XML Object reference",x);
    }
    
    
    try
    { super.message(context,message,path);
    }
    finally
    { channel.pop();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void render(EventContext context)
    throws IOException
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
          =AbstractXmlObject.<Treferent>create(type.getURI(),instanceURI,null);
      }
      
      channel.push(reference.get());
    }
    catch (BindException x)
    { throw new RuntimeException("Error creating XML Object reference",x);
    }
    
    try
    { renderChildren(context);
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
    throws BindException
  { 
    if (reference==null)
    { reference=AbstractXmlObject.<T>create(typeURI,instanceURI,null);
    }

    return reference;
  }
}

