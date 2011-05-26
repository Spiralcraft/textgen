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

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.text.Renderer;
import spiralcraft.text.Wrapper;
import spiralcraft.textgen.OutputContext;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.kit.AbstractMessageHandler;

import spiralcraft.textgen.RenderMessage;

/**
 * A MessageHandler which performs simple rendering of wrapped content
 * 
 * @author mike
 */
public class WrapperHandler
  extends AbstractMessageHandler
{
  
  private final Wrapper wrapper;
  
  public WrapperHandler(Wrapper wrapper)
  { this.wrapper=wrapper;
  }
  
  { type=RenderMessage.TYPE;
  }

  @Override
  protected void doHandler(
    final Dispatcher dispatcher,
    final Message message,
    final MessageHandlerChain next)
  {
    try
    { 
      wrapper.render
      (OutputContext.get()
      ,new Renderer()
      {
        @Override
        public void render(Appendable writer)
          throws IOException
        { next.handleMessage(dispatcher,message);
        }
      }
      );
    }
    catch (IOException x)
    { throw new RuntimeException("Error rendering",x);
    }
    
  }
}
