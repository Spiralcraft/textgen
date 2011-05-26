//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.textgen.kit;

import java.io.IOException;

import spiralcraft.text.Renderer;

import spiralcraft.app.Message;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.kit.AbstractMessageHandler;

import spiralcraft.textgen.OutputContext;
import spiralcraft.textgen.RenderMessage;

/**
 * A MessageHandler which performs simple rendering
 * 
 * @author mike
 */
public class RenderHandler
  extends AbstractMessageHandler
{
  
  private final Renderer renderer;
  
  public RenderHandler(Renderer renderer)
  { this.renderer=renderer;
  }

  protected RenderHandler()
  { this.renderer=null;
  }
  
  { type=RenderMessage.TYPE;
  }

  @Override
  protected void doHandler(
    Dispatcher dispatcher,
    Message message,
    MessageHandlerChain next)
  {
    try
    { render(dispatcher);
    }
    catch (IOException x)
    { throw new RuntimeException("Error rendering",x);
    }
    next.handleMessage(dispatcher,message);
  }
  
  protected void render(Dispatcher dispatcher)
    throws IOException
  { 
    if (renderer!=null)
    { renderer.render( OutputContext.get() );
    }
    else
    { throw new IllegalStateException
        ("RendererHandler must be constructed with a Renderer or must "
        +" or must override the render() method"
        );
    }
  }

}
