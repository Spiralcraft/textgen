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

import java.util.LinkedList;

import spiralcraft.app.Message;
import spiralcraft.app.State;
import spiralcraft.sax.XmlWriter;

import org.xml.sax.ContentHandler;

/**
 * <p>Provides all Elements in the document tree access to the rendering
 *   context across a complete rendering cycle. Provides 
 *   a mechanism for Elements to share state between events.
 * </p>
 * 
 * @author mike
 */
public class EventContext
{
  @SuppressWarnings("unused")
  private final EventContext parent;
  private final Appendable output;
  private ContentHandler contentHandler;
  private State elementState;
  private final boolean stateful;
  private String logPrefix;
  private StateFrame currentFrame;
  private LinkedList<Integer> messagePath;
  
  /**
   * <p>Create a GenerationContext that does not refer to any ancestors,
   *  and sends output to the specified Writer.
   * </p>
   * 
   * <p>If a StateFrame is not provided, a new one will be created
   * </p>
   */
  public EventContext(Appendable output,boolean stateful,StateFrame frame)
  { 
    this.parent=null;
    this.output=output;
    this.stateful=stateful;
    this.currentFrame=frame;
    
    if (currentFrame==null && stateful)
    { currentFrame=new StateFrame();
    }
  }
  
  
  /**
   * Create a GenerationContext that refers to its ancestors for the resolution
   *   of dependencies.
   * 
   * @param parent The parent GenerationContext
   */
  public EventContext(Appendable output,EventContext parent)
  { 
    this.parent=parent;
    this.output=output;
    this.stateful=parent.isStateful();
    currentFrame=parent.getCurrentFrame();
  }
  
  /**
   * Provide a new StateFrame to trigger a State refresh
   * 
   * @param frame
   */
  public void setCurrentFrame(StateFrame frame)
  { this.currentFrame=frame;
  }
  
  /** 
   * @return The ContentHandler to which XML output will be rendered
   */
  public ContentHandler getContentHandler()
  { 
    if (contentHandler==null)
    { contentHandler=new XmlWriter(output,null);
    }
    return contentHandler;
  }
  
  /** 
   * @return The Writer to which output will be rendered
   */
  public Appendable getOutput()
  { return output;
  }
  
  
  /**
   * 
   * @return The state of the current Element, set by this Element's parent
   *   via setState()
   */
  public State getState()
  { return elementState;
  }

  /**
   * Provide the ElementState associated with a child element, immediately
   *   before that childElement is rendered or messaged.
   * 
   * @param state
   */
  public void setState(State state)
  { elementState=state;
  }
  
  public void dispatch
    (Message message
    ,Element root
    ,LinkedList<Integer> messagePath
    )
  { dispatch(message,root,elementState,messagePath);
  }
  
  public void dispatch
    (Message message
    ,Element root
    ,State state
    ,LinkedList<Integer> messagePath
    )
  {
    State lastState=this.elementState;
    LinkedList<Integer> lastMessagePath=messagePath;
    try
    {
      this.elementState=state;
      this.messagePath=messagePath;
      root.message(this,message);
    }
    finally
    { 
      messagePath=lastMessagePath;
      this.elementState=lastState;
    }
  }
  
  public Integer getNextRoute()
  {
    if (messagePath==null || messagePath.isEmpty())
    { return null;
    }
    else
    { return messagePath.getFirst();
    }
  }
  /**
   * Indicate whether the current message path still has unreached elements
   * 
   * @return
   */
  public boolean isRelayingMessage()
  { return messagePath!=null && !messagePath.isEmpty();
  }
  
  
  public final void descend(int index)
  { 
    if (messagePath!=null && !messagePath.isEmpty())
    { 
      Integer element=messagePath.removeFirst();
      if (element!=index)
      { 
        throw new RuntimeException
          ("Route violation: "+index+" != next segment "+element);
      };
    }
    if (this.elementState!=null)
    { this.elementState=this.elementState.getChild(index);
    }
  }
  
  public final void ascend()
  { 
    if (this.elementState!=null)
    { this.elementState=this.elementState.getParent();
    }
  }  
  
  
  /**
   * Relay a message to a child component
   * 
   * @param childComponent
   * @param childIndex
   * @param message
   */
  public void relayMessage
    (Element childComponent,int childIndex,Message message)
  {
    if (isStateful() && elementState!=null)
    {
      
      State childState=elementState.getChild(childIndex);
      if (childState==null)
      { 
        childState=childComponent.createState();
        elementState.setChild(childIndex,childState);    
      }
      
      final State lastState=this.elementState;
      
      descend(childIndex);
      try
      { childComponent.message(this,message);
      }
      finally
      {
        ascend();
        this.elementState=lastState;
      }
    }
    else
    { childComponent.message(this,message);
    }
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
  
  public StateFrame getCurrentFrame()
  { return currentFrame;
  }
  
  protected void setLogPrefix(String logPrefix)
  { this.logPrefix=logPrefix;
  }
  
  
}
