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

import spiralcraft.util.StringUtil;

import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.Assembly;
import spiralcraft.builder.PropertySpecifier;
import spiralcraft.builder.BuildException;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;

import spiralcraft.xml.Attribute;
import spiralcraft.xml.ParserContext;
import spiralcraft.xml.TagReader;

import java.net.URI;

import java.io.Writer;
import java.io.IOException;

import spiralcraft.textgen.Element;

import spiralcraft.text.markup.Unit;
import spiralcraft.text.markup.MarkupUnit;
import spiralcraft.text.markup.MarkupException;

/**
 * A Unit which represents an Element delimited by start and end tag(s) or
 *   signified by an empty tag
 */
public class ElementUnit
  extends MarkupUnit
  implements TglUnit
{
  private static final URI _DEFAULT_ELEMENT_PACKAGE
    =URI.create("java:/spiralcraft/textgen/elements/");
  
  private final CharSequence _code;
  private boolean _open=true;
  private AssemblyClass _assemblyClass;
  private URI _elementPackage;
  private String _elementName;
  private Attribute[] _attributes;
  
  private Expression _expression;
  
  public ElementUnit(CharSequence code)
    throws MarkupException
  { 
    super(code);
    _open=!(code.charAt(code.length()-1)=='/');
    if (!_open)
    { code=code.subSequence(0,code.length()-1);
    }

    
    _code=code;
    
    if (_code.charAt(0)=='=')
    { readExpressionElement();
    }
    else
    { readStandardElement();
    }
    if (!_open)
    { close();
    }
  }
  
  private void readExpressionElement()
    throws MarkupException
  { 
    CharSequence expressionText;
    if (_code.charAt(1)=='=')
    { expressionText=_code.subSequence(2,_code.length());
    }
    else
    { expressionText=_code.subSequence(1,_code.length());
    }
    
    try
    { _expression=Expression.parse(expressionText.toString());
    }
    catch (spiralcraft.lang.ParseException x)
    { throw new MarkupException(x);
    }
  }
  
  private void readStandardElement()
    throws MarkupException
  { 
    
    try
    {
      ParserContext context=new ParserContext(_code.toString());
      TagReader tagReader=new TagReader();
      tagReader.readTag(context);
          
      
      String name=tagReader.getTagName();
      setName(name);
      int nspos=name.indexOf(':');
      if (nspos>-1)
      { 
        _elementPackage=resolveNamespace(name.substring(0,nspos));
        _elementName=name.substring(nspos+1);
      }
      else
      { 
        _elementPackage=_DEFAULT_ELEMENT_PACKAGE;
        _elementName=name;
      }
      _attributes=tagReader.getAttributes();
    }
    catch (spiralcraft.xml.ParseException x)
    { throw new MarkupException(x);
    }

  }
  
  private URI resolveNamespace(String namespaceId)
    throws MarkupException
  { throw new MarkupException("Unknown namespace "+namespaceId);
  }
  
  public boolean isOpen()
  { return _open;
  }

  public void close()
    throws MarkupException
  {
    _open=false;
    if (_expression==null)
    {
      try
      {
        _assemblyClass=new AssemblyClass
          (null
          ,_elementPackage
          ,Character.toUpperCase(_elementName.charAt(0))+_elementName.substring(1)
          ,null
          ,null
          );

        if (_attributes!=null)
        { 
          for (int i=0;i<_attributes.length;i++)
          { 
            _assemblyClass.addPropertySpecifier
              (new PropertySpecifier
                (_assemblyClass
                ,_attributes[i].getName()
                ,_attributes[i].getValue()
                )
              );
          }
        }
        _assemblyClass.resolve();
      }
      catch (BuildException x)
      { throw new MarkupException(x);
      }
    }
  }

  public Element bind(Assembly parent,Element parentElement)
    throws BuildException,BindException
  { 
    if (_expression!=null)
    { 
      Element element=new ExpressionElement();
      element.bind(parentElement);
      return element;
    }
    else
    {
      Assembly assembly=_assemblyClass.newInstance(parent);
      Element element=(Element) assembly.getSubject().get();
      
      element.bind(parentElement);
      bindChildren(assembly,element);
      return element;
    }
  }

  private void bindChildren(Assembly assembly,Element element)
    throws BuildException,BindException
  {
    Unit[] children=getChildren();
    if (children.length>0)
    { 
      Element[] childElements=new Element[children.length];
      for (int i=0;i<children.length;i++)
      { 
        
        if (children[i] instanceof TglUnit)
        { childElements[i]=((TglUnit) children[i]).bind(assembly,element);
        }
      }
      element.setChildren(childElements);
    }
  }
  
  
  class ExpressionElement
    extends Element
  { 
    
    private Channel _source;
    
    public void bind(Element parent)
      throws BindException
    { 
      super.bind(parent);
      _source=getFocus().bind(_expression);
    }
    
    public void write(Writer out)
      throws IOException
    { 
      Object value=_source.get();
      if (value!=null)
      { out.write(value.toString());
      }
    }
  }
}
