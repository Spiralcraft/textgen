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
import spiralcraft.textgen.RenderingContext;

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
  
  private final TglCompiler compiler;
  private final CharSequence code;
  private boolean open=true;
  private ElementFactory elementFactory;
  private URI elementPackage;
  private String elementName;
  private Attribute[] attributes;
  
  private Expression<?> expression;
  
  public TglElementUnit
    (TglCompiler compiler
    ,CharSequence code
    ,ParsePosition position
    )
    throws ParseException
  { 
    this.compiler=compiler;
    setPosition(position);
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
      ParsePosition position=getPosition().clone();
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
  { throw new MarkupException("Unknown namespace "+namespaceId,getPosition());
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
      elementFactory=compiler.createElementFactory
        (elementPackage,elementName,attributes,getPosition());
    }
  }

  public Element bind(Element parentElement)
    throws MarkupException
  { 
    if (expression!=null)
    { 
      Element element=new ExpressionElement();
      try
      { element.bind(parentElement,children);
      }
      catch (BindException x)
      { throw new MarkupException(x.toString(),getPosition(),x);
      }
      return element;
    }
    else
    {
      Element element=elementFactory.createElement(parentElement);
      try
      { element.bind(parentElement,children);
      }
      catch (BindException x)
      { throw new MarkupException(x.toString(),getPosition(),x);
      }
      return element;
    }
  }

  class ExpressionElement
    extends Element
  { 
    
    private Channel<?> _source;
    
    @SuppressWarnings("unchecked") // Heterogeneous use of lang package
    public void bind(Element parent,List<TglUnit> children)
      throws BindException,MarkupException
    { 
      super.bind(parent,children);
      try
      {
        _source=getFocus().bind(expression);
      }
      catch (BindException x)
      { 
        throw new MarkupException
          ("Error binding '"+expression+"': "+x.toString()
          ,getPosition()
          ,x
          );
      }
    }
    
    public void write(RenderingContext context)
      throws IOException
    { 
      Object value=_source.get();
      if (value!=null)
      { context.getWriter().write(value.toString());
      }
    }
  }
}
