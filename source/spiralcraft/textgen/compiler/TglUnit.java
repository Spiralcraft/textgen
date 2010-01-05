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

import java.io.PrintWriter;

import spiralcraft.common.namespace.PrefixResolver;

import spiralcraft.lang.BindException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;


import spiralcraft.text.ParseException;
import spiralcraft.text.markup.Unit;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.text.xml.Attribute;

import java.io.IOException;
import java.util.HashMap;

/**
 * A Unit of text generation which represents a
 *   node in the tree structure of a TGL block.
 */
public abstract class TglUnit
  extends Unit<TglUnit>
{
  
  protected boolean allowsChildren=true;
  protected boolean trim;
  protected boolean debug;

  private HashMap<String,DefineUnit> defines;
  
  public TglUnit(TglUnit parent)
  { super(parent);
  }
  
  public void define(String name,DefineUnit unit)
  {
    if (defines==null)
    { defines=new HashMap<String,DefineUnit>();
    }
    
    defines.put(name, unit);
  }
  
  public DefineUnit findDefinition(String name)
  {
    // Parent takes precedence
    DefineUnit ret=parent!=null?parent.findDefinition(name):null;
    if (ret==null && defines!=null)
    { ret=defines.get(name);
    }
    return ret;
      
//    // Local takes precedence 
//    // (deprecated- parent precedence facilitates inheritance)
//    DefineUnit ret=defines!=null?defines.get(name):null;
//    if (ret==null && parent!=null)
//    { return parent.findDefinition(name);
//    }
//    return ret;
  }
  
  public boolean allowsChildren()
  { return allowsChildren;
  }
    
  /**
   * <P>Create a tree of Elements bound into an application context
   *   (the Assembly) which implements the functional behavior 
   *   specified by the TGL document.
   */
  public abstract Element bind(Element parentElement)
    throws MarkupException;

  public void dumpTree(PrintWriter writer,String linePrefix)
  { 
    writer.println(linePrefix+toString());
    for (TglUnit unit: children)
    { unit.dumpTree(writer,linePrefix+"  ");
    }
  }  
  
  protected Element defaultBind(Element parentElement)
    throws MarkupException
  { 
    Element element=new DefaultElement();
    element.setParent(parentElement);
    try
    { element.bind(children);
    }
    catch (BindException x)
    { throw new MarkupException(x.toString(),getPosition(),x);
    }
    return element;
  }
  
  protected boolean checkUnitAttribute(Attribute attrib)
    throws ParseException
  {
    if (attrib.getName().startsWith("textgen:"))
    { 
      
      addUnitAttribute
        (attrib.getName().substring(8),attrib.getValue());
      return true;

    }
    return false;
  }
  
  protected void addUnitAttribute(String name,String value)
    throws ParseException
  { 
    name=name.intern();
    if (name.equals("trim"))
    { trim=Boolean.parseBoolean(value);
    }
    else if (name.equals("debug"))
    { debug=Boolean.parseBoolean(value);
    }
    else
    { 
      throw new ParseException
        ("Unrecognized textgen attribute '"+name
        +"' - not one of {trim,debug}"
        ,getPosition()
        );
    }
    
  }
  
  /**
   * <p>Whether whitespace should be trimmed from any content blocks directly
   *   contained in this Unit.
   * </p>
   * 
   * <p>The default value is false, but this directive will be handled
   *   according to the specific subtype of TglUnit in use.
   * </p>
   * 
   * @return Whether to trim whitespace or not
   */
  public boolean getTrim()
  { return trim;
  }
  
  /**
   * 
   * @return The NamespaceResolver which provides namespace mappings
   *   currently in effect.
   */
  public PrefixResolver getNamespaceResolver()
  { 
    if (parent!=null)
    { return parent.getNamespaceResolver();
    }
    return null;
  }  
  
}

class DefaultElement
  extends Element
{
  @Override
  public void render(EventContext context)
    throws IOException
  { renderChildren(context);
  }
}
