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

import spiralcraft.app.Message;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.MessageHandler;
import spiralcraft.textgen.MessageHandlerChain;

/**
 * A MessageHandler with a no-op bind() implementation to use for simple
 *   extension.
 * 
 * @author mike
 */
public abstract class AbstractMessageHandler
  implements MessageHandler
{

  protected Message.Type type;
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  { return focusChain;
  }

  @Override
  public final void handleMessage
    (EventContext dispatcher,Message message,MessageHandlerChain next)
  {
    if (shouldHandleMessage(dispatcher,message))
    { doHandler(dispatcher,message,next);
    }
    else
    { next.handleMessage(dispatcher,message);
    }
  }
  
  protected abstract void
    doHandler
      (EventContext dispatcher
      ,Message message
      ,MessageHandlerChain next
      );
  
  
  protected boolean shouldHandleMessage(EventContext dispatcher,Message message)
  { return (type==null || type==message.getType());
  }
  
  @Override
  public Message.Type getType()
  { return type;
  }

}
