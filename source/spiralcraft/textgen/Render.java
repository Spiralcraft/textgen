//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.textgen;

import java.io.IOException;
import java.net.URI;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.ClosureFocus;
import spiralcraft.text.ParseException;
import spiralcraft.vfs.util.ByteArrayResource;

/**
 * A ChannelFactory that converts input to text by running it through
 *   a textgen template
 *   
 * @author mike
 *
 */
public class Render<T>
  implements ChannelFactory<String,T>
{
  private URI templateURI;
  private String templateText;
  
  private Generator generator;
  private ClosureFocus<T> focus;

  /**
   * The location of the template to render against the input
   * 
   * @param templateURI
   */
  public void setTemplateURI(URI templateURI)
  { this.templateURI=templateURI;
  }
  
  /**
   * The literal template code to render against the input
   * 
   * @param templateText
   */
  public void setTemplateText(String templateText)
  { this.templateText=templateText;
  }
  
  @Override
  public Channel<String> bindChannel(
    Channel<T> source,
    Focus<?> context,
    Expression<?>[] arguments)
    throws BindException
  {
    focus=new ClosureFocus<T>(context,source);
    try
    { 
      if (templateURI!=null)
      { generator=new Generator(templateURI,focus);
      }
      else if (templateText!=null)
      { 
        generator
          =new Generator(new ByteArrayResource(templateText.getBytes()),focus);
      }
      generator.bind(focus);
    }
    catch (IOException x)
    { throw new BindException("Error loading template "+templateURI,x);
    }
    catch (ParseException x)
    { throw new BindException("Error loading template "+templateURI,x);
    }    
    return new RenderChannel();
    
  }
  
  class RenderChannel
    extends AbstractChannel<String>
  {
    public RenderChannel()
    { super(BeanReflector.<String>getInstance(String.class));
    }
    
    @Override
    public String retrieve()
    { 
      focus.push();
      try
      { return generator.render();
      }
      catch (IOException x)
      { throw new AccessException("Error rendering template",x);
      }
      finally
      { focus.pop();
      }
    }

    @Override
    protected boolean store(
      String val)
      throws AccessException
    { throw new UnsupportedOperationException
        ("Rendering is not reversible");
    }
  }

}