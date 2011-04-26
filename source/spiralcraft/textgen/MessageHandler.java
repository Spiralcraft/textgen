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
package spiralcraft.textgen;


import spiralcraft.app.Message;
import spiralcraft.lang.Contextual;

public interface MessageHandler
  extends Contextual
{
  /**
   * <p>Handle the Message. This will be called twice- once before children are
   *   messaged and once afterwards.
   * </p>
   * 
   * @param context The EventContext that holds the Element's state
   * @param message The message
   * @param postOrder Whether this call is before or after the message has
   *                    propagated to children 
   */
  void handleMessage
    (EventContext context
    ,Message message
    ,MessageHandlerChain next
    );
  
  /**
   * <p>The type of message this handler supports, or null if all 
   *   message types will be sent through this handler.
   * </p>
   * 
   * @return
   */
  Message.Type getType();  
}
