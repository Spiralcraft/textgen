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
package spiralcraft.textgen;

import spiralcraft.util.thread.ThreadLocalStack;

/**
 * A reference to the Appendable used by textgen components to render
 *   output.
 * 
 * @author mike
 *
 */
public class OutputContext
{


  public static final ThreadLocalStack<Appendable> APPENDER_LOCAL
    =new ThreadLocalStack<Appendable>();
  
  public static final void push(Appendable appendable)
  { APPENDER_LOCAL.push(appendable);
  }
  
  public static final void pop()
  { APPENDER_LOCAL.pop();
  }
  
  public static final Appendable get()
  { return APPENDER_LOCAL.get();
  }
  
}
