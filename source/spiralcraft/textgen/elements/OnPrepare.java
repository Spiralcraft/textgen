//
// Copyright (c) 1998,2007 Michael Toth
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

import spiralcraft.app.Message;
import spiralcraft.app.components.OnMessage;
import spiralcraft.textgen.PrepareMessage;


/**
 * <p>Evaluates an expression on the Prepare stage of the request
 * </p>
 * 
 * @author mike
 *
 */
public class OnPrepare
  extends OnMessage
{

  { super.setMessageType(PrepareMessage.TYPE);
  }
  
  @Override
  public void setMessageType(Message.Type messageType)
  { 
    throw new UnsupportedOperationException
      ("Message type cannot be changed from PrepareMessage.TYPE");
  }
}



