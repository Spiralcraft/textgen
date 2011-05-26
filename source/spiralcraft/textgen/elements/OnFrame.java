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

import spiralcraft.lang.Binding;
import spiralcraft.textgen.kit.StandardElement;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.MessageHandlerChain;
import spiralcraft.app.kit.FrameHandler;


/**
 * <p>Evaluates an expression when the StateFrame changes
 * </p>
 * 
 * @author mike
 *
 */
public class OnFrame
  extends StandardElement
{
  private Binding<?> binding;
  
  {
    addHandler(new FrameHandler()
      {
        @Override
        public void doHandler(Dispatcher dispatcher,Message message,MessageHandlerChain chain)
        { binding.get();
        }
      }
    );
  }
  

  /**
   * <p>An expression to evaluate when the StateFrame changes
   * </p>
   * 
   * @param expression
   */
  public void setX(Binding<?> binding)
  {
    removeParentContextual(this.binding);
    this.binding=binding;
    addParentContextual(this.binding);
  }
}



