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

import java.io.Writer;


/**
 * <p>Provides Elements with access to the rendering context as well as
 *   a mechanism for Elements to share state between events.
 * </p>
 * 
 * @author mike
 */
public class EventContext
{
  private final EventContext parent;
  private Writer writer;
  private ElementState elementState;
  private final boolean stateful;
  private String logPrefix;
  
  /**
   * Create a GenerationContext that does not refer to any ancestors, and sends
   *   output to the specified Writer.
   */
  public EventContext(Writer writer,boolean stateful)
  { 
    this.parent=null;
    this.writer=writer;
    this.stateful=stateful;
  }
  
  /**
   * Create a GenerationContext that refers to its ancestors for the resolution
   *   of dependencies.
   * 
   * @param parent The parent GenerationContext
   */
  public EventContext(EventContext parent)
  { 
    this.parent=parent;
    this.writer=this.parent.getWriter();
    this.stateful=parent.isStateful();
  }
  
  /** 
   * @return The Writer to which output will be rendered
   */
  public Writer getWriter()
  { return writer;
  }
  
  
  /** 
   * Provide the Writer to which output will be rendered
   */
  public void setWriter(Writer writer)
  { this.writer=writer;
  }
  
  /**
   * 
   * @return The state of the current Element, set by this Element's parent
   *   via setState()
   */
  public ElementState getState()
  { return elementState;
  }

  /**
   * Provide the ElementState associated with a child element, immediately
   *   before that childElement is rendered or messaged.
   * 
   * @param state
   */
  public void setState(ElementState state)
  { elementState=state;
  }
  
  /**
   * <p>A stateful rendering or messaging allows for direct
   *   manipulation of document content, but costs memory and CPU.
   * </p>
   * 
   * @return Whether ElementStates should be created and maintained for
   *   components.
   */
  public boolean isStateful()
  { return stateful;
  }
  
  public String getLogPrefix()
  { return logPrefix;
  }
  
  protected void setLogPrefix(String logPrefix)
  { this.logPrefix=logPrefix;
  }
  
}
