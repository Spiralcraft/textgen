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

/**
 * Instructs Elements to render output to the writer in the EventContext 
 * 
 * @author mike
 */
public class RenderMessage
    extends Message
{
  public static final Type TYPE=new Type();
  public static final RenderMessage INSTANCE=new RenderMessage();
  
  { multicast=true;
  }
  
  @Override
  public Type getType()
  { return TYPE;
  }
  
  
}
