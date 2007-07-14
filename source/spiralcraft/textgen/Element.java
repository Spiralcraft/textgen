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
package spiralcraft.textgen;

import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

import spiralcraft.builder.Assembly;

import java.io.IOException;

import java.util.List;

import spiralcraft.textgen.compiler.TglUnit;


import java.net.URI;

/**
 * <P>A unit of output in a TGL document.
 * 
 * <P>An Element may contain other Elements and/or content, forming a tree of
 *   Elements which generate output.
 *   
 * <P>Elements are multi-threaded, and thus should not maintain execution
 *   state internally. An element must resolve its state within the write()
 *   method, and may use the GenerationContext object to make items available
 *   to sub-elements.
 *   
 * <P>The Elements associate with application specific runtime context via the
 *   Focus, which is provided to the root Element by the container. Elements may
 *   extend this Focus to refine the context for their child elements. The
 *   spiralcraft.lang expression language is used to access application context
 *   through this Focus.
 *   
 * <P>Elements are beans which are instantiated and configured 
 *   (ie. parameterized) using the spiralcraft.builder package. Each Element
 *   is associated with an Assembly. The Assembly provides a means for 
 *   Elements to associate with other elements in the Element structure
 *   without those elements being adjacent to each other.
 * 
 * <P>Elements are created by first instantiating them and applying the bean
 *   properties specified in their TGL declarations. The Element is then bound
 *   to its already bound parent Element, where it is able to bind any
 *   expressions to the Focus provided by its parent element. 
 */
public abstract class Element
{ 
  private Element[] children;
  private Element parent;
  private Assembly<?> assembly;
  private String id;

  /**
   * Specify an id for this Element and make it visible to the expression
   *   language when used anywhere inside this element. 
   */
  public void setId(String id)
  { this.id=id;
  }
  
  public String getId()
  { return id;
  }
  
  /**
   * @return The Focus associated with this Element. Defaults to the parent
   *   Element's Focus, unless overridden.
   */
  public Focus<?> getFocus()
  { 
    if (parent!=null)
    { return parent.getFocus();
    }
    return null;
  }
  
  /**
   * @return The context URI associated with this Element, which is generally
   *   the URI of the directory that contains the TGL source file. 
   */
  public URI getContextURI()
  {
    if (parent!=null)
    { return parent.getContextURI();
    }
    return null;
  }

  /**
   * @return The Assembly from which this Element was instantiated
   */
  public Assembly<?> getAssembly()
  { return assembly;
  }
  
  /**
   * Specify the Assembly from which this Element was instantiated. Used
   *   internally only.
   */
  public void setAssembly(Assembly<?> assembly)
  { this.assembly=assembly;
  }
  
  /**
   * Called when binding Units. This method should call
   *   TglUnit.bind() on the child units at an appropriate time. The default
   *   behavior is to bind all the childUnits.
   */
  public void bind(Element parent,List<TglUnit> childUnits)
    throws BindException
  { 
    this.parent=parent;
    bindChildren(childUnits);
  }

  protected void bindChildren
    (List<TglUnit> childUnits
    )
    throws BindException
  {
    if (childUnits!=null)
    { 
      children=new Element[childUnits.size()];
      int i=0;
      for (TglUnit child: childUnits)
      { children[i++]=child.bind(this);
      }
    }
  }

  protected void writeChildren(GenerationContext context)
    throws IOException
  {
    if (children!=null)
    { 
      for (int i=0;i<children.length;i++)
      { children[i].write(context);
      }
    }
  }

  /**
   * Recursively perform processing and write output
   */
  public abstract void write(GenerationContext context)
    throws IOException;
}
