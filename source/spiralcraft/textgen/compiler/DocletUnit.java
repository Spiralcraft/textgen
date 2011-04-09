//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.textgen.compiler;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;

import spiralcraft.text.markup.Unit;
import java.io.IOException;

import java.net.URI;

import spiralcraft.vfs.Resource;

import java.util.ArrayList;

/**
 * A compilation unit (ie. a file or other container) of tgl markup.
 * 
 */
public class DocletUnit
  extends TglUnit
{
  protected final Resource resource;
  private final ArrayList<DocletUnit> subDocs
    =new ArrayList<DocletUnit>();
  
  
  public DocletUnit(TglUnit parent,Resource resource)
  { 
    super(parent);
    this.resource=resource;
    initPrefixResolver();

    if (parent!=null)
    {
      DocletUnit parentDoc=parent.findUnit(DocletUnit.class);
      if (parentDoc!=null)
      { parentDoc.registerSubDoclet(this);
      }
    }
    
  }
  
  @Override
  public String getName()
  { return "doclet";
  }
  
  public URI getSourceURI()
  { return resource.getURI();
  }
  
  void registerSubDoclet(DocletUnit subDoc)
  { subDocs.add(subDoc);
  }
  
  @SuppressWarnings({"unchecked","rawtypes"}) // Downcast from runtime check
  @Override
  public Unit findUnit(Class clazz)
  {
    if (clazz==NamespaceUnit.class)
    { 
      // Stop resolving namespaces within the document
      return null;
    }
    else
    { return super.findUnit(clazz);
    }
  }
  
  /**
   * @return The modification time of the most recently modified resource
   *   in the tree.
   */
  public long getLastModified()
    throws IOException
  { 
    long time=resource.getLastModified();
    for (DocletUnit subDoc:subDocs)
    { time=Math.max(time,subDoc.getLastModified());
    }
    return time;
  }
  
  
  /**
   * Finds a unit that is an ancestor in the containership hierarchy within
   *   the scope of the current document.
   * 
   * @param unitClass
   * @return
   */
  @SuppressWarnings("unchecked")
  @Override
  public <X extends TglUnit> X findUnitInDocument(Class<X> unitClass)
  { 
    if (getClass().isAssignableFrom(unitClass))
    { return (X) this;
    }
    else return null;
  }
  
  @Override
  public Element createElement()
  { return new RootElement();
  }
  
    
  class RootElement
    extends Element
  {
    
    @Override
    public URI getContextURI()
    { return resource.getURI();
    }
    
   
    @Override
    public void render(EventContext context)
      throws IOException
    { renderChildren(context);
    }
    
  
  }
  
}
