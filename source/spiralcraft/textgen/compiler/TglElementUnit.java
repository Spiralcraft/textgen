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



import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.Assembly;
import spiralcraft.builder.PropertySpecifier;
import spiralcraft.builder.BuildException;

import spiralcraft.lang.Expression;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;

import spiralcraft.xml.Attribute;
import spiralcraft.xml.ParserContext;
import spiralcraft.xml.TagReader;

import java.net.URI;

import java.io.IOException;

import java.util.List;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.GenerationContext;

import spiralcraft.text.markup.MarkupException;

import spiralcraft.text.ParseException;
import spiralcraft.text.ParsePosition;

/**
 * A Unit which represents an Element delimited by start and end tag(s) or
 *   signified by an empty tag
 */
public class TglElementUnit
  extends TglUnit
{
  private static final URI _DEFAULT_ELEMENT_PACKAGE
    =URI.create("java:/spiralcraft/textgen/elements/");
  
  private final CharSequence code;
  private boolean open=true;
  private AssemblyClass assemblyClass;
  private URI elementPackage;
  private String elementName;
  private Attribute[] attributes;
  
  private Expression<?> expression;
  private ParsePosition position;
  
  public TglElementUnit(CharSequence code,ParsePosition position)
    throws ParseException
  { 
    this.position=position;
    open=!(code.charAt(code.length()-1)=='/');
    if (!open)
    { code=code.subSequence(0,code.length()-1);
    }

    
    this.code=code;
    
    if (code.charAt(0)=='=')
    { readExpressionElement();
    }
    else
    { readStandardElement();
    }
    if (!open)
    { close();
    }
  }
  
  private void readExpressionElement()
    throws MarkupException
  { 
    CharSequence expressionText;
    if (code.charAt(1)=='=')
    { expressionText=code.subSequence(2,code.length());
    }
    else
    { expressionText=code.subSequence(1,code.length());
    }
    
    try
    { expression=Expression.parse(expressionText.toString());
    }
    catch (spiralcraft.lang.ParseException x)
    { 
      position.setContext(expressionText);
      throw new MarkupException(position,x);
    }
  }
  
  private void readStandardElement()
    throws ParseException
  { 
    
    ParserContext context=new ParserContext(code.toString());
    TagReader tagReader=new TagReader();
    tagReader.readTag(context);


    String name=tagReader.getTagName();
    setName(name);
    int nspos=name.indexOf(':');
    if (nspos>-1)
    { 
      elementPackage=resolveNamespace(name.substring(0,nspos));
      elementName=name.substring(nspos+1);
    }
    else
    { 
      elementPackage=_DEFAULT_ELEMENT_PACKAGE;
      elementName=name;
    }
    attributes=tagReader.getAttributes();

  }
  
  private URI resolveNamespace(String namespaceId)
    throws MarkupException
  { throw new MarkupException("Unknown namespace "+namespaceId,position);
  }
  
  public boolean isOpen()
  { return open;
  }

  public void close()
    throws MarkupException
  {
    open=false;
    if (expression==null)
    {
      String elementClassName=elementName;
      try
      {
        assemblyClass=new AssemblyClass
          (null
          ,elementPackage
          ,elementClassName
          ,null
          ,null
          );

        if (attributes!=null)
        { 
          for (int i=0;i<attributes.length;i++)
          { 
            assemblyClass.addPropertySpecifier
              (new PropertySpecifier
                (assemblyClass
                ,attributes[i].getName()
                ,attributes[i].getValue()
                )
              );
          }
        }
        assemblyClass.resolve();
      }
      catch (BuildException x)
      { 
        if  (x.getCause() instanceof ClassNotFoundException)
        { 
          throw new MarkupException
            (elementPackage+elementClassName+" does not resolve to an" +
             " Assembly or a Class"
            ,position
            );
        }
        else
        { throw new MarkupException(position,x);
        }
      }
    }
  }

  public Element bind(Element parentElement)
    throws BindException
  { 
    if (expression!=null)
    { 
      Element element=new ExpressionElement();
      element.bind(parentElement,children);
      return element;
    }
    else
    {
      try
      {
        Assembly<?> parentAssembly=parentElement.getAssembly();
        Assembly<?> assembly=assemblyClass.newInstance(parentAssembly);
        Element element=(Element) assembly.getSubject().get();
        element.setAssembly(assembly);
      
        element.bind(parentElement,children);
        return element;
      }
      catch (BuildException x)
      { throw new BindException("Error instantiating Element: "+x,x);
      }
    }
  }

  class ExpressionElement
    extends Element
  { 
    
    private Channel<?> _source;
    
    @SuppressWarnings("unchecked") // Heterogeneous use of lang package
    public void bind(Element parent,List<TglUnit> children)
      throws BindException
    { 
      super.bind(parent,children);
      _source=getFocus().bind(expression);
    }
    
    public void write(GenerationContext context)
      throws IOException
    { 
      Object value=_source.get();
      if (value!=null)
      { context.getWriter().write(value.toString());
      }
    }
  }
}
