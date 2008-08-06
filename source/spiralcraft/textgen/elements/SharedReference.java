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
import java.util.List;

import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.persist.AbstractXmlObject;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.CompoundFocus;
import spiralcraft.lang.Focus;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.compiler.TglUnit;

/**
 * <p>Exposes an object reference via the Focus chain, via the
 *   spiralcraft.data.persist mechanism. The lifecycle of the object
 *   is scoped to the bound resource, and the object is shared among
 *   all individual state trees and renderings.
 * </p>
 * 
 * @author mike
 *
 * @param <Treferent>
 */
public class SharedReference<Treferent>
    extends Element
{
  private Channel<Treferent> channel;
  private CompoundFocus<Treferent> focus;
  private Type<?> type;
  private URI instanceURI;
  private AbstractXmlObject<Treferent,?> reference;
  
  
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
    
    reference
      =AbstractXmlObject.<Treferent>create(type.getURI(),instanceURI,null);
    
    channel=reference.bind(parentFocus);
    
    focus=new CompoundFocus(parentFocus,channel);
    focus.bindFocus(getId(),getAssembly().getFocus());
    
    super.bind(childUnits);
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
  public Focus<Treferent> getFocus()
  { return focus;
  }

  @Override
  public void render(EventContext context)
    throws IOException
  { renderChildren(context);
  }
  
}



