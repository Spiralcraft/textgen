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
import spiralcraft.log.ClassLogger;
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
  private static final ClassLogger log
    =ClassLogger.getInstance(InsertUnit.class);
  
  private String referencedName;
  private boolean require=false;
  
  public InsertUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,Attribute[] attribs
    )
    throws MarkupException,ParseException
  { 
    super(parent);
    allowsChildren=true;

    for (Attribute attrib: attribs)
    {
      if (attrib.getName().equals("name"))
      { this.referencedName=attrib.getValue();
      }
      else if (attrib.getName().equals("require"))
      { require=Boolean.parseBoolean(attrib.getValue());
      }
      else if (attrib.getName().startsWith("textgen:"))
      { super.addUnitAttribute(attrib.getName().substring(8),attrib.getValue());
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
  
  
  @Override
  public String getName()
  { return "@insert";
  }
  
  @Override
  public Element bind(Element parentElement)
    throws MarkupException
  {
    if (referencedName!=null)
    {
      DefineUnit defineUnit=findDefinition(referencedName);
      if (defineUnit!=null)
      { 
        if (debug)
        { log.fine("Binding define unit '"+referencedName+"'");
        }
        return defineUnit.bindContent(parentElement);
      }
      else if (!require)
      { 
        if (debug)
        { log.fine("Binding default for '"+referencedName+"'");
        }
        // Render default
        InsertElement element=new InsertElement();
        element.setParent(parentElement);
        try
        { element.bind(children);
        }
        catch (BindException x)
        { throw new MarkupException(x.toString(),getPosition());
        }
        return element;
        
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
    
      InsertIncludeElement element=new InsertIncludeElement();
      element.setParent(parentElement);
      try
      { element.bind(children);
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
  
  @Override
  public void bind(List<TglUnit> children)
    throws BindException,MarkupException
  { 
    ancestorInclude=findElement(IncludeElement.class);
    super.bind(children);
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