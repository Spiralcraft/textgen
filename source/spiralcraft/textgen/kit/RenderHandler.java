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

import spiralcraft.app.Message;
import spiralcraft.text.Renderer;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.MessageHandlerChain;
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
    EventContext dispatcher,
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
  
  protected void render(EventContext dispatcher)
    throws IOException
  { 
    if (renderer!=null)
    { renderer.render(dispatcher.getOutput());
    }
    else
    { throw new IllegalStateException
        ("RendererHandler must be constructed with a Renderer or must "
        +" or must override the render() method"
        );
    }
  }

}
