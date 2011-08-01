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

import spiralcraft.app.Component;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.Event;
import spiralcraft.app.Message;
import spiralcraft.app.State;
import spiralcraft.app.StateFrame;

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
  implements Dispatcher
{
  
  
  
  
  @SuppressWarnings("unused")
  private final EventContext parent;
  private final Appendable output;
  private ContentHandler contentHandler;
  private State state;
  private final boolean stateful;
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
    currentFrame=parent.getFrame();
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
  @Override
  public State getState()
  { return state;
  }
  
  @Override
  public void handleEvent(Event event)
  {
  }

  /**
   * Provide the ElementState associated with a child element, immediately
   *   before that childElement is rendered or messaged.
   * 
   * @param state
   */
  public void setState(State state)
  { this.state=state;
  }
  
  public void dispatch
    (Message message
    ,Element root
    ,LinkedList<Integer> messagePath
    )
  { dispatch(message,root,state,messagePath);
  }
  
  public void dispatch
    (Message message
    ,Element root
    ,State startingState
    ,LinkedList<Integer> messagePath
    )
  {
    State lastState=this.state;
    LinkedList<Integer> lastMessagePath=messagePath;
    OutputContext.push(output);
    if (startingState!=null && !message.isOutOfBand())
    { startingState.enterFrame(currentFrame);
    }
    try
    {
      this.state=startingState;
      this.messagePath=messagePath;
      root.message(this,message);
    }
    finally
    { 
      if (startingState!=null && !message.isOutOfBand())
      { startingState.exitFrame();
      }
      OutputContext.pop();
      messagePath=lastMessagePath;
      this.state=lastState;
    }
  }
  
  @Override
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
   * Indicate whether the current component is within the set of targets
   *   for the current message, i.e. there are no more route segments to
   *   process
   * 
   * @return
   */
  @Override
  public boolean isTarget()
  { return messagePath==null || messagePath.isEmpty();
  }
  
  @Override
  public final void descend(int index,boolean outOfBand)
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
    if (this.state!=null)
    { 
      this.state=this.state.getChild(index);
      if (this.state==null)
      { throw new IllegalStateException("Child state must exist");
      }
      if (!outOfBand)
      { this.state.enterFrame(currentFrame);
      }
    }
  }
  
  @Override
  public final void ascend(boolean outOfBand)
  { 
    if (this.state!=null)
    { 
      if (!outOfBand)
      { this.state.exitFrame();
      }
      this.state=this.state.getParent();
    }
  }  
  
  
  /**
   * Relay a message to a child component
   * 
   * @param childComponent
   * @param childIndex
   * @param message
   */
  @Override
  public void relayMessage
    (Component childComponent,int childIndex,Message message)
  {
    if (isStateful() && state!=null)
    {
      
      State childState=state.getChild(childIndex);
      if (childState==null)
      { 
        childState=childComponent.createState();
        state.setChild(childIndex,childState);    
      }
      
      final State lastState=this.state;
      
      descend(childIndex,message.isOutOfBand());
      try
      { childComponent.message(this,message);
      }
      finally
      {
        ascend(message.isOutOfBand());
        this.state=lastState;
      }
    }
    else
    { childComponent.message(this,message);
    }
  }  
  
  @Override
  public void relayMessage
    (Component childComponent
    ,State newParentState
    ,int childIndex
    ,Message message
    )
  {  
    final State lastState=this.state;
    this.state=newParentState;
    
    if (lastState!=null && newParentState==null)
    { 
      throw new IllegalArgumentException
        ("newParentState cannot be null");
    }
    
    try
    { relayMessage(childComponent,childIndex,message);
    }
    finally
    { this.state=lastState;
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
  @Override
  public boolean isStateful()
  { return stateful;
  }
  
  @Override
  public StateFrame getFrame()
  { return currentFrame;
  }

  
  @Override
  public String getContextInfo()
  { return null;
  }
}
