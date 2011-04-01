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

import java.io.IOException;
import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.text.ParseException;
import spiralcraft.text.markup.MarkupException;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;


import spiralcraft.text.xml.Attribute;

/**
 * A Unit which defines a content subtree or a value for insertion elsewhere.
 */
public class DefineUnit
  extends ProcessingUnit
{
  
  private String publishedName;
  private boolean imported;
  private String tagName;
  private boolean inDoclet;
  
  public DefineUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,String importName
    )
  { 
    super(parent,compiler.getPosition());
    publishedName=importName;
    imported=true;
    debug=parent.debug;
    inDoclet=parent instanceof DocletUnit;
    
    
  }
  
  public DefineUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,Attribute[] attribs
    ,String tagName
    )
    throws MarkupException,ParseException
  { 
    super(parent,compiler.getPosition());
    this.tagName=tagName;
    if (tagName.startsWith("$"))
    { publishedName=tagName.substring(1);
    }
    allowsChildren=true;
    
    for (Attribute attrib: attribs)
    {
      if (attrib.getName().equals("name"))
      { this.publishedName=attrib.getValue();
      }
      else if (attrib.getName().equals("resource"))
      { includeResource(attrib.getValue(),compiler);
      }
      else if (attrib.getName().equals("export"))
      { this.setExported(Boolean.valueOf(attrib.getValue()));
      }
      else if (attrib.getName().equals("imports"))
      { 
        String[] imports=attrib.getValue().split(",");
        for (String name : imports)
        {
          name=name.trim();
          define
            (name
            ,new DefineUnit
              (this
              ,compiler
              ,name
              )
            );
        }
        
      }
      else if (super.checkUnitAttribute(attrib))
      {
      }      
      else
      { 
        throw new MarkupException
          ("Attribute '"+attrib.getName()+"' not in {resource,value,export}"
          ,compiler.getPosition()
          );
      }
    }
    
    if (publishedName==null)
    { throw new MarkupException
        ("Attribute 'name' required for @define",compiler.getPosition());
    }
    parent.define(publishedName,this);
    

  }
  
  public boolean isImported()
  { return imported;
  }
  

  
  public String getPublishedName()
  { return publishedName;
  }  
  
  @Override
  public String getName()
  { return tagName;
  }
  
  @Override
  /**
   * The DefineUnit does not bind into it's container. Binding is deferred
   *   so the content can be bound into the referencing Insert unit.
   */
  public Element bind(Focus<?> focus,Element parentElement)
    throws MarkupException
  { 
    NullElement ret=new NullElement(parentElement);
    try
    { ret.bind(focus,null);
    }
    catch (BindException x)
    { throw new MarkupException("Error binding null element",getPosition(),x);
    }
    return ret;
    
  }
  
  /**
   * Find the bound reference to this DefineUnit within the ancestral
   *   element tree, in order to resolve contextual information (imports,
   *   overlay children) from the binding site.
   *   
   * @param child
   * @return
   */
  public DefineElement findBoundElement(Element child)
  {
    DefineElement ret=null;
    while (child!=null)
    {
      ret=child.findElement(DefineElement.class);
      if (ret.isFromUnit(this))
      { break;
      }
      child=ret.getParent();
      ret=null;
    }
    return ret;
  }
  
  /**
   * Called from an insert unit to bind an instance of the defined subtree to 
   *   the location of use.
   *   
   * The overlay is content contained within the referencing insert unit, and
   *   will be if/where the defined unit contains an inner insert.
   *   
   *   
   * If this element is imported, the actual DefineElement that will be
   *   used will be retrieved from the overlay
   *   
   * @param focus
   * @param parentElement
   * @param overlay
   * @return
   * @throws MarkupException
   */
  public Element bindContent
    (Focus<?> focus,Element parentElement,List<TglUnit> overlay)
    throws MarkupException
  {
    
    
    
    // If this is an imported define, the parentElement will be the parent
    //   of the insert that referenced us, which is in the local set.
    //   We can find the DefineElement that binds our block into the target
    //   location and look up our import in its children. 
    
    if (imported)
    { 

      if (!inDoclet)
      {
        DefineElement boundContainer
          =((DefineUnit) parent).findBoundElement(parentElement);
      
        if (boundContainer!=null)
        { 
      
          for (TglUnit child : boundContainer.getOverlay())
          {
            if (debug)
            { log.fine("Checking overlay for import '"+publishedName+"': "
                +child);
            }
        
            if (child instanceof DefineUnit)
            { 
              DefineUnit subst=(DefineUnit) child;
              if (subst.getPublishedName().equals(publishedName))
              { return subst.bindContent(focus,parentElement,children);
              }
            }
          }
        }
        else
        { 
          // We're being bound directly
        }
      }
      else
      {
        TglUnit includer=parent.getParent();
        if (includer!=null)
        {
          for (TglUnit child : includer.getChildren())
          {
            if (debug)
            { log.fine("Checking overlay for import '"+publishedName+"': "
                +child);
            }
        
            if (child instanceof DefineUnit)
            { 
              DefineUnit subst=(DefineUnit) child;
              if (subst.getPublishedName().equals(publishedName))
              { return subst.bindContent(focus,parentElement,children);
              }
            }                  
          }
        }
      }
    }

    if (imported && debug)
    {
      log.debug("Could not resolve imported '"+publishedName+"'");
    }
    
    DefineElement element=new DefineElement(parentElement,this,overlay);
    try
    { element.bind(focus,children);
    }
    catch (BindException x)
    { throw new MarkupException(x.toString(),getPosition());
    }
    
    return element;
  }
  
  public Element bindExtension(Attribute[] attribs,Focus<?> focus,Element parentElement,List<TglUnit> children)
    throws MarkupException
  { 
    if (attribs!=null && attribs.length>0)
    { throw new MarkupException("Unrecognized attribute "+attribs[0].getName(),getPosition());
    }
    return bindContent(focus,parentElement,children);
  }
  
  
}

class DefineElement
  extends Element
{
  
  private final DefineUnit unit;
  private final List<TglUnit> overlay;
  
  public DefineElement
    (Element parentElement,DefineUnit unit,List<TglUnit> overlay)
  { 
    super(parentElement);
    this.setCodePosition(unit.getPosition());
    this.unit=unit;
    this.overlay=overlay;
  }
  

  
  public List<TglUnit> getOverlay()
  { return overlay;
  }
  
  public boolean isFromUnit(DefineUnit unit)
  { return this.unit==unit;
  }
  
  @Override
  public void render(EventContext context)
    throws IOException
  { renderChildren(context);
  }

}