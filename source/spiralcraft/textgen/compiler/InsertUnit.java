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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;
import spiralcraft.text.ParseException;
import spiralcraft.text.markup.MarkupException;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.Message;


import spiralcraft.text.xml.Attribute;

/**
 * A Unit which inserts the contents of an ancestral IncludeUnit, or a unit referenced by a defined name
 */
public class InsertUnit
  extends ProcessingUnit
{
  private static final ClassLog log
    =ClassLog.getInstance(InsertUnit.class);
  

  private String referencedName;
  private String tagName;
  private boolean require=false;
  private Attribute[] attributes;
  
  public InsertUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,Attribute[] attribs
    ,String tagName
    )
    throws MarkupException,ParseException
  { 
    super(parent,compiler.getPosition());
    allowsChildren=true;
    this.tagName=tagName;
    
    
    if (tagName.startsWith("@"))
    {
      // Form <%@insert ...
      for (Attribute attrib: attribs)
      {
        if (attrib.getName().equals("name"))
        { this.referencedName=attrib.getValue();
        }
        else if (attrib.getName().equals("require"))
        { require=Boolean.parseBoolean(attrib.getValue());
        }
        else if (super.checkUnitAttribute(attrib))
        {
        }
        else
        { 
          throw new MarkupException
            ("Attribute '"+attrib.getName()+"' not in {name,require}"
            ,compiler.getPosition()
            );
        }
      }
    }
    else
    {
      // Form <%refname ...
      if (this.referencedName==null)
      {
        this.referencedName=resolvePrefixedName(tagName,null).toString();
        this.require=true;
      }
      
      ArrayList<Attribute> otherAttribs=new ArrayList<Attribute>();
      for (Attribute attrib: attribs)
      {
        if (attrib.getName().equals("require"))
        { require=Boolean.parseBoolean(attrib.getValue());
        }
        else if (!super.checkUnitAttribute(attrib))
        { otherAttribs.add(attrib);
        }
      }
      this.attributes=otherAttribs.toArray(new Attribute[otherAttribs.size()]);
    }   
  }
  
  
  @Override
  public String getName()
  { return tagName;
  }
  
  /**
   * Binds this element's content
   * 
   * @param focus
   * @param parentElement
   * @return
   */
  public InsertElement bindContent(Focus<?> focus,Element parentElement)
    throws MarkupException,BindException
  { return (InsertElement) bind(focus,parentElement,new InsertElement());
  }
  
  @Override
  public Element bind(Focus<?> focus,Element parentElement)
    throws MarkupException
  {
    if (referencedName!=null)
    {
      // This is a directive to insert a named reference to another component
      
      TglUnit referencedUnit=findDefinition(referencedName);
      
      if (referencedUnit!=null)
      { 
        if (debug)
        { log.fine("Binding referenced unit '"+referencedName+"'");
        }
        referencedUnit.exportDefines(this);
        return referencedUnit.bindExtension(attributes,focus,parentElement,children);
      }
      else if (!require)
      { 
        if (debug)
        { log.fine("Binding default for '"+referencedName+"'");
        }
        // Render default self contents
        
        try
        { return bindContent(focus,parentElement);
        }
        catch (BindException x)
        { throw new MarkupException(x.toString(),getPosition(),x);
        }
        
      }
      else
      { 
        throw new MarkupException
          ("Fragment '"+referencedName+"' not found."
          ,getPosition()
          );
      }
    }
    else
    {
      // If the insert is contained in a Define unit in this document,
      //   then it should render the children of the containing "insert"
      //   reference.
      Element element=null;
      DefineUnit defineUnit=this.findUnitInDocument(DefineUnit.class);
      if (defineUnit!=null)
      {  
        element=new InsertIncludeElement(defineUnit);
        
      }
      else
      { element=new InsertIncludeElement();
      }
      return bind(focus,parentElement,element);
    }
  }
  
  
}

class InsertElement
  extends Element
{
  
  @Override
  public void render(EventContext context)
    throws IOException
  { renderChildren(context);
  }
}


class InsertIncludeElement
  extends Element
{
  private IncludeElement ancestorInclude;
  private DefineUnit defineUnit;
  private DefineElement ancestorInsert;
  
  public InsertIncludeElement()
  { super();
  }
  
  public InsertIncludeElement(DefineUnit defineUnit)
  { this.defineUnit=defineUnit;
  }  
  
  @Override
  public Focus<?> bind(Focus<?> focus)
    throws BindException,MarkupException
  { 
    
    List<TglUnit> children=getScaffold().getChildren();
    if (defineUnit!=null)
    {
      // If the anonymous insert is inside a define unit, bind the default
      //   children of the insert that referenced the define.
        
      // Find the insert that referenced our define
      ancestorInsert=defineUnit.findBoundElement(getParent());
    }
    
    if (ancestorInsert!=null)
    { children=ancestorInsert.getOverlay();
    }
    else
    {
      // Get the nearest containing Include that is not in the same 
      //   document.
    
      Element containingDocument
        =findElement(DocletUnit.RootElement.class);
    
      if (containingDocument!=null)
      { ancestorInclude=containingDocument.findElement(IncludeElement.class);
      }
    
    }

    super.bindChildren(focus,children);
    return focus;
  }
  
  @Override
  public void message
    (EventContext context
    ,Message message
    ,LinkedList<Integer> path
    )
  {    
    if (ancestorInclude!=null)
    { ancestorInclude.messageClosure(context,message,path);
    }
    else 
    { super.message(context, message, path);
    }


  }
  
  @Override
  public void render(EventContext context)
    throws IOException
  { 
    if (ancestorInclude!=null)
    { ancestorInclude.renderClosure(context);
    }
    else 
    { renderChildren(context);
    }
    
  }

}