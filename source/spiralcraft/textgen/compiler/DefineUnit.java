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

import java.util.List;

import spiralcraft.app.Parent;
import spiralcraft.common.ContextualException;
import spiralcraft.common.namespace.QName;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Setter;
import spiralcraft.lang.kit.ConstantChannel;
import spiralcraft.lang.util.DictionaryBinding;
import spiralcraft.text.ParseException;
import spiralcraft.text.markup.MarkupException;

import spiralcraft.textgen.Element;


import spiralcraft.text.xml.Attribute;

/**
 * A Unit which defines a content subtree or a value for insertion elsewhere.
 */
public class DefineUnit
  extends ProcessingUnit
{
  
  private String publishedName;
  private final boolean virtual;
  private String tagName;
  private boolean inDoclet;
  private Expression<?> contextX;
  
  public DefineUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,String importName
    )
    throws ParseException
  { 
    super(parent,compiler.getPosition());
    publishedName
      =resolvePrefixedName
        (importName,TglUnit.DEFAULT_ELEMENT_PACKAGE).toString();
    virtual=true;
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
    this.virtual=false;
    this.tagName=tagName;
    if (tagName.startsWith("$"))
    { publishedName
        =resolvePrefixedName
          (tagName.substring(1),TglUnit.DEFAULT_ELEMENT_PACKAGE).toString();
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
            (resolvePrefixedName
              (name,TglUnit.DEFAULT_ELEMENT_PACKAGE).toString()
            ,new DefineUnit
              (this
              ,compiler
              ,name
              )
            );
        }
        
      }
      else if (attrib.getName().equals("x"))
      { 
        
        try
        { contextX=Expression.parse(attrib.getValue());
        }
        catch (spiralcraft.lang.ParseException x)
        { 
          throw new ParseException
            ("Error parsing attribute 'x' context expression",getPosition(),x);
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
  
  public String getPublishedName()
  { return publishedName;
  }  
  
  @Override
  public String getName()
  { return tagName;
  }
  
  /**
   * The DefineUnit does not bind into it's container. Binding is deferred
   *   so the content can be bound into the referencing Insert unit.
   */
  @Override
  public Element createElement()
  { return new NullElement();
  }
  
  
  /**
   * Find the bound reference to this DefineUnit within the ancestral
   *   element tree, in order to resolve contextual information (imports,
   *   overlay children) from the binding site.
   *   
   * @param child
   * @return
   */
  public DefineElement findBoundElement(Parent parent)
  {
    DefineElement ret=null;
    while (parent!=null)
    {
      ret=parent.findComponent(DefineElement.class);
      if (ret==null)
      { throw new IllegalStateException("No DefineElement is parent of "+parent);
      }
      if (ret.isFromUnit(this))
      { break;
      }
      parent=ret.getParent();
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
    (Attribute[] attribs
    ,Focus<?> focus
    ,Element parentElement
    ,List<TglUnit> overlay
    )
    throws ContextualException
  {
    
    
   
    if (virtual)
    { 
      // If this is an imported (virtual) define, it is a placeholder that will
      //   resolve another Define from within the importing Element.
      //
      //  We can find the real DefineElement with the same name inside the 
      //   container that references our parent container.

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
              { return subst.bindContent(attribs,focus,parentElement,children);
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
        // We're a virtual define at the top level of a tgl doclet
        
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
              { return subst.bindContent(attribs,focus,parentElement,children);
              }
            }                  
          }
        }
      }
    }

    if (virtual && debug)
    {
      log.debug("Could not resolve imported '"+publishedName+"'");
    }
    
    DefineElement element=new DefineElement(this,overlay);
    if (contextX!=null)
    { focus=bindContext(focus,attribs);
    }
    else if (attribs!=null && attribs.length>0)
    { throw new MarkupException("Unrecognized attribute "+attribs[0].getName(),getPosition());
    }    
    
    return bind(focus,parentElement,element);
  }
  
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private Focus<?> bindContext(Focus<?> focus,Attribute[] attribs)
    throws MarkupException
  {
    Channel<?> context;
    try
    { context=new ConstantChannel(focus.bind(contextX));
    }
    catch (BindException x)
    { 
      throw new MarkupException
        ("Error binding context expression for define",getPosition(),x);
    }
    Focus<?> contextFocus=focus.chain(context);
    contextFocus.addAlias(new QName(publishedName).toURIPath());
    focus=focus.chain(focus.getSubject());
    focus.addFacet(contextFocus);
    
    if (attribs!=null)
    {
      for (Attribute attrib:attribs)
      { 
        String name=attrib.getName();
        try
        {
          if (name.startsWith("$"))
          { 
            Channel source=focus.bind(Expression.create(attrib.getValue()));
            if (!source.isConstant())
            { 
              throw new MarkupException
                ("Attribute "+attrib.getName()
                +" expression `"+attrib.getValue()+"` is not constant, and"
                +" cannot be used for bind-time context"
                ,getPosition()
                );
            }
            
            Setter setter
              =new Setter
                (source
                ,contextFocus.bind(Expression.create(name.substring(1)))
                );
            if (!setter.set())
            {
              throw new MarkupException
                ("Attribute "+attrib.getName()
                +" could not be applied",getPosition()
                );
            }
            
          }
          else
          {
            DictionaryBinding attribBinding
              =new DictionaryBinding(attrib.getName());
            attribBinding.bind(contextFocus);
            attribBinding.set(attrib.getValue());
          }
        }
        catch (BindException x)
        {
          throw new MarkupException
            ("Error binding context expression for define",getPosition(),x);
        }
        catch (spiralcraft.lang.ParseException x)
        {
          throw new MarkupException
            ("Error in attribute name",getPosition(),x);
        }
        
      }
    }
    return focus;
  }
  
  @Override
  public Element bindExtension
    (Attribute[] attribs
    ,Focus<?> focus
    ,Element parentElement
    ,List<TglUnit> children
    )
    throws ContextualException
  { 
    
    // The attributes are 'values' for the formal parameters
    //
    // In this case, the attribute values should be treated as expressions
    //   for a struct that will be put into the context.
    //
    // The struct will be defined by this Define element. 
    //
    // The expressions are evaluated on bind, or according to whatever the
    //   field definition is.
    //
    // Some expressions can be defined as textual substitutions? 
    return bindContent(attribs,focus,parentElement,children);
  }
  
  
}

class DefineElement
  extends Element
{
  
  private final DefineUnit unit;
  private final List<TglUnit> overlay;
  
  public DefineElement
    (DefineUnit unit,List<TglUnit> overlay)
  { 
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

}