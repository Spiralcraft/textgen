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
 * A Unit which inserts the contents of an ancestral IncludeUnit
 */
public class InsertUnit
  extends ProcessingUnit
{
  private static final ClassLog log
    =ClassLog.getInstance(InsertUnit.class);
  

  private DefineUnit referencedDefine;
  private String referencedName;
  private String tagName;
  private boolean require=false;
  
  public InsertUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,Attribute[] attribs
    ,String tagName
    ,DefineUnit referencedDefine
    )
    throws MarkupException,ParseException
  { 
    super(parent);
    allowsChildren=true;
    this.tagName=tagName;
    this.referencedDefine=referencedDefine;
    

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
    if (this.referencedName==null && !tagName.startsWith("@"))
    { this.referencedName=tagName;
    }
    
    
    if (this.referencedDefine==null && this.referencedName!=null)
    { this.referencedDefine=findDefinition(this.referencedName);
    }
    
    if (this.referencedDefine!=null)
    { this.referencedDefine.exportDefines(this);
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
  { 
    InsertElement element=new InsertElement(parentElement);
    element.bind(focus,children);
    return element;
  }
  
  @Override
  public Element bind(Focus<?> focus,Element parentElement)
    throws MarkupException
  {
    if (referencedName!=null)
    {
      DefineUnit defineUnit=referencedDefine;
      if (defineUnit==null)
      { 
        log.info("Late binding of insert '"+referencedName+"'");
        defineUnit=findDefinition(referencedName);
      }
      
      if (defineUnit!=null)
      { 
        if (debug)
        { log.fine("Binding define unit '"+referencedName+"'");
        }
        return defineUnit.bindContent(focus,parentElement,children);
      }
      else if (!require)
      { 
        if (debug)
        { log.fine("Binding default for '"+referencedName+"'");
        }
        // Render default
        
        try
        { return bindContent(focus,parentElement);
        }
        catch (BindException x)
        { throw new MarkupException(x.toString(),getPosition());
        }
        
      }
      else
      { 
        throw new MarkupException
          ("Name '"+referencedName+"' not found."
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
        element=new InsertIncludeElement(parentElement,defineUnit);
        
      }
      else
      { element=new InsertIncludeElement(parentElement);
      }
      try
      { element.bind(focus,children);
      }
      catch (BindException x)
      { throw new MarkupException(x.toString(),getPosition());
      }
      return element;
    }
  }
  
  
}

class InsertElement
  extends Element
{
  public InsertElement(Element parent)
  { super(parent);
  }
  
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
  
  public InsertIncludeElement(Element parent)
  { super(parent);
  }
  
  public InsertIncludeElement(Element parent,DefineUnit defineUnit)
  { 
    super(parent);
    this.defineUnit=defineUnit;
  }  
  
  @Override
  public void bind(Focus<?> focus,List<TglUnit> children)
    throws BindException,MarkupException
  { 
    
    
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

    super.bind(focus,children);
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