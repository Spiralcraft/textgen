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

import spiralcraft.text.xml.Attribute;
import spiralcraft.text.xml.ParserContext;
import spiralcraft.text.xml.TagReader;

import java.net.URI;

import java.io.IOException;

import java.util.List;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;


import spiralcraft.text.markup.MarkupException;

import spiralcraft.text.ParseException;
import spiralcraft.text.ParsePosition;

import spiralcraft.log.ClassLogger;

/**
 * A Unit which represents an Element delimited by start and end tag(s) or
 *   signified by an empty tag
 */
public class ElementUnit
  extends TglUnit
{
  private static final ClassLogger log=new ClassLogger(ElementUnit.class);
  
  private static final URI _DEFAULT_ELEMENT_PACKAGE
    =URI.create("java:/spiralcraft/textgen/elements/");
  
  private final TglCompiler<?> compiler;
  private final CharSequence code;
  private ElementFactory elementFactory;
  private URI elementPackage;
  private String elementName;
  private Attribute[] attributes;
  private boolean open;
  
  private Expression<?> expression;
  
  public ElementUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,CharSequence code
    ,ParsePosition position
    )
    throws ParseException
  { 
    super(parent);
    this.compiler=compiler;
    setPosition(position.clone());
    
    /*
    open=!(code.charAt(code.length()-1)=='/');
    if (!open)
    { code=code.subSequence(0,code.length()-1);
    }
    */
    
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
    if (code.charAt(code.length()-1)!='/')
    { 
      throw new MarkupException
        ("Expression tag must be empty (close with '/>')"
        ,getPosition().clone()
        );
    }
    
    if (code.charAt(1)=='=')
    { expressionText=code.subSequence(2,code.length()-1);
    }
    else
    { expressionText=code.subSequence(1,code.length()-1);
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
    open=false;
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
    open=!tagReader.isClosed();

  }
  
  
  private URI resolveNamespace(String namespaceId)
    throws MarkupException
  {
    // Called via the constructor
    
    URI namespaceURI=null;
    NamespaceUnit unit=this.findUnit(NamespaceUnit.class);
    if (unit!=null)
    { namespaceURI=unit.resolveNamespace(namespaceId);
    }
    if (namespaceURI==null)
    { throw new MarkupException("Unknown namespace "+namespaceId,getPosition());
    }
    else
    { return namespaceURI;
    }
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
      element.setParent(parentElement);
      try
      { element.bind(children);
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
      { element.bind(children);
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
    
    @Override
    @SuppressWarnings("unchecked") // Heterogeneous use of lang package
    public void bind(List<TglUnit> children)
      throws BindException,MarkupException
    { 
      super.bind(children);
      try
      {
        _source=getFocus().bind(expression);
      }
      catch (BindException x)
      { 
        log.fine(getFocus().toString());
        throw new MarkupException
          ("Error binding '"+expression+"': "+x.toString()
          ,getPosition()
          ,x
          );
      }
    }
    
    public void render(EventContext context)
      throws IOException
    { 
      Object value;
      try
      { value=_source.get();
      }
      catch (NullPointerException x)
      { value=null;
      }
        
      if (value!=null)
      { context.getWriter().write(value.toString());
      }
    }
  }
}
