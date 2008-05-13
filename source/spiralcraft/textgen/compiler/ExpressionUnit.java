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
 * A Unit which contains an expression for output
 */
public class ExpressionUnit
  extends TglUnit
{
  private static final ClassLogger log=ClassLogger.getInstance(ElementUnit.class);
  
  public static final URI DEFAULT_ELEMENT_PACKAGE
    =URI.create("class:/spiralcraft/textgen/elements/");
  
  private boolean open;
  
  private Expression<?> expression;
  private CharSequence markup;
  
  public ExpressionUnit
    (TglUnit parent
    ,CharSequence markup
    ,ParsePosition position
    )
    throws ParseException
  { 
    super(parent);  
    setPosition(position);
    this.markup=markup;
    readExpressionElement();
    if (!open)
    { close();
    }
  }

  public boolean isOpen()
  { return open;
  }
  
  /**
   * An element with a tag in the form <code>&lt;%= <i>expression</i> %&gt;</code> 
   * 
   * @throws ParseException
   */
  private void readExpressionElement()
    throws MarkupException
  { 
    CharSequence expressionText;
    if (markup.charAt(markup.length()-1)!='/')
    { 
      throw new MarkupException
        ("Expression tag must be empty (close with '/>')"
        ,getPosition().clone()
        );
    }
    
    if (markup.charAt(1)=='=')
    { expressionText=markup.subSequence(2,markup.length()-1);
    }
    else
    { expressionText=markup.subSequence(1,markup.length()-1);
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
    
  public Expression<?> getExpression()
  { return expression;
  }
  
  
  /**
   * <p>Notify ElementUnit of a close tag.
   * </p>
   * 
   * <p>Provides an opportunity for an ElementUnit to 
   *   integrate its content.
   * </p>
   *   
   */
  public void close()
    throws MarkupException
  { open=false;
  }

  
  public Element bind(Element parentElement)
    throws MarkupException
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
        log.warning("Caught "+x.toString()+"\r\n    focus="+getFocus().getFocusChain().toString());
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
      { 
        x.printStackTrace();
        value=null;
      }
        
      if (value!=null)
      { context.getWriter().write(value.toString());
      }
    }
  }
}
