//
// Copyright (c) 2008,2008 Michael Toth
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

import spiralcraft.lang.Channel;

/**
 * <p>Handles a message by evaluating an Expression 
 * </p>
 * 
 * @author mike
 */
public class ExpressionMessageHandler
  implements MessageHandler
{

  private Channel<?> channel;
  private boolean postOrder;
  
  public ExpressionMessageHandler(Channel<?> channel,boolean postOrder)
  {
    this.channel=channel;
    this.postOrder=postOrder;
  }
  
  @Override
  public void handleMessage
    (EventContext context, Message message,boolean postOrder)
  { 
    if (this.postOrder==postOrder)
    { channel.get();
    }
  }

}
