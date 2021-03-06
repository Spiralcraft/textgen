//
// Copyright (c) 2008,2009 Michael Toth
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
package spiralcraft.textgen.elements;

import java.io.IOException;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.kit.AbstractMessageHandler;
import spiralcraft.app.MessageHandlerChain;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.util.DictionaryBinding;

import spiralcraft.text.xml.AttributeEncoder;
import spiralcraft.text.xml.XmlEncoder;

import spiralcraft.textgen.ExpressionFocusElement;
import spiralcraft.textgen.OutputContext;
import spiralcraft.textgen.RenderMessage;

/**
 * <p>Encodes the contents as XML, nested in a tag with the specified name
 * </p>
 * 
 * @author mike
 */
public class XMLEncode<T>
  extends ExpressionFocusElement<T>
{
  
  private String name;
  private DictionaryBinding<?>[] attributeBindings;
  private DictionaryBinding<?> contentBinding;
  
  @SuppressWarnings("rawtypes") // We don't care
  private Expression contentExpression;
  
  private AttributeEncoder attributeEncoder = new AttributeEncoder();
  private XmlEncoder contentEncoder = new XmlEncoder();
  private boolean hasContent;
  
  { addHandler(new XMLRenderHandler());
  }
  
  /**
   * The tag name
   * 
   * @param name
   */
  public void setName(String name)
  { this.name=name;
  }
  
  /**
   * The tag attribute definitions
   * 
   * @param bindings
   */
  public void setAttributeBindings(DictionaryBinding<?>[] bindings)
  { this.attributeBindings=bindings; 
  }
  
  /**
   * <p>The expression to evaluate and XML encode which will be output between
   *   the begin and end tags of the this XML Element. 
   * </p>
   * 
   * @param bindings
   */
  public void setContentX(Expression<?> contentX)
  { this.contentExpression=contentX; 
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  protected Focus<?> bindExports(Focus<?> focusChain) 
    throws BindException 
  { 
    if (attributeBindings!=null)
    { 
      for (DictionaryBinding<?> binding : attributeBindings)
      { binding.bind(focusChain);
      }
    }
    if (contentExpression!=null)
    { 
      contentBinding=new DictionaryBinding();
      contentBinding.setTarget(contentExpression);
      hasContent=true;
    }
    return focusChain;
  }


  private void renderAttributes(Appendable writer)
    throws IOException
  { 
    if (attributeBindings!=null)
    { 
      for (DictionaryBinding<?> binding:attributeBindings)
      {
        String value=binding.get();
        if (value!=null)
        { 
          writer.append(" ");
          writer.append(binding.getName());
          writer.append(" = \"");
          attributeEncoder.encode(binding.get(),writer);
          writer.append("\"");
        
        }
      }
      
    }
    
  }
  
  private void renderContent()
    throws IOException
  {
    if (contentBinding!=null)
    { contentEncoder.encode(contentBinding.get(),OutputContext.get());
    }
      
  }
  
  class XMLRenderHandler
    extends AbstractMessageHandler
  {
    { type=RenderMessage.TYPE;
    }
    
    @Override
    public void doHandler
      (Dispatcher context,Message message,MessageHandlerChain next) 
    {
      try
      {
        Appendable writer=OutputContext.get();
          
        writer.append("<");
        writer.append(name);
        
        renderAttributes(writer);
        
        if (hasContent)
        { 
          writer.append(">");
          renderContent();
        }
        else
        { writer.append("/>");
        }
          
        next.handleMessage(context,message);
    
        if (hasContent)
        {
          writer.append("</");
          writer.append(name);
          writer.append(">");
        }
      }
      catch (IOException x)
      { throw new RuntimeException("Error rendering XML",x);
      }
    }

    
  }

  
}
