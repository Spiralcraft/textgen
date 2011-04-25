//
//Copyright (c) 1998,2007 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.textgen;

import spiralcraft.app.State;

/**
 * <p>Represents the state of an Element in association with a specific 
 *   context, such as a user or a particular source of data, for the purpose
 *   of manipulating a document once the content has been generated.
 * </p>
 *   
 * <h3>Elements and State</h3>
 * 
 * <p>The tree of Elements that make up a textgen document comprises the
 *   abstract document structure. A tree of ElementStates, in conjunction with
 *   the data bindings associated with the Elements, provide the content
 *   of the document- the concrete document structure.
 * </p>
 *   
 * <p>In the simple case, an Element may be rendered in a stateless fashion. 
 *   The Element may be rendering multiple documents simultanously that
 *   reference different data, using different threads. Because the Element
 *   is stateless, no extra memory is consumed as additional threads
 *   simultaneously render the document.
 * </p>
 * 
 * <p>In some cases, it is desirable for an Element to keep track of some
 *   stateful information so that this information can be manipulated or
 *   processed before the document is generated, or so the document can be
 *   generated multiple times with minor changes without repeating expensive
 *   computations. For example, this functionality is broadly applicable to
 *   data entry mechanisms which use text-based output such as web based forms
 *   or character based user interfaces. The ElementState class provides a 
 *   container for this state that is managed by the EventContext that Elements
 *   access through a variety of methods.
 * </p>
 *   
 * <p>In many cases, a single Element is rendered multiple times within a
 *   document in the context of an iteration of some sort. For each element 
 *   in the iteration, a rendering takes place that outputs different data.
 *   In this case, for each rendering, a different ElementState object and
 *   resulting subtree may be created. This is necessary in order to provide
 *   proper context when an event is directed at one of the many
 *   items in an iteration- for example, the manipulation of a 'detail' line
 *   item in a document. 
 * </p>
 *   
 * <h3>Data structure</h3>
 * 
 * <p>ElementState objects are organized into a tree structure.
 *   An ElementState is always associated with an instance of an Element,
 *   and is normally referenced by the parent Element's ElementState using a
 *   simple array that parallels the parent Element's child list.
 * </p>
 * 
 * <p>An Element retrieves its ElementState from the EventContext, which tracks
 *   progress through the Element tree and maintains a reference to the 
 *   ElementState that corresponds to the current in-process Element.
 * </p>
 * 
 * <p>When an Element is contained in some form of Iteration, the element that
 *   does the Iteration will store an intermediate ElementState subtree for
 *   each unit of the Iteration.
 * </p>
 * 
 * @author mike
 *
 */
public class ElementState
  implements State
{

  private State parent;
  private final State[] children;
  private int[] path;
  private volatile StateFrame currentFrame;
  
  protected ElementState()
  { children=null;
  }
  
  public ElementState(int numChildren)
  { 
    // System.err.println("ElementState: new "+toString());
    this.children=new ElementState[numChildren];
  }
  
  
  @Override
  public void link(State parent,int[] path)
  {
    if (this.parent!=null)
    { 
      throw new IllegalStateException
        ("Already linked: cannot change parent state");
    }
    this.parent=parent;
    this.path=path;
  }
  
  void setParent(ElementState parent)
  { this.parent=parent;
  }
  
  void setPath(int[] path)
  { this.path=path;
  }
  
  /**
   * 
   * @return The path from the root of the ElementState tree 
   */
  @Override
  public int[] getPath()
  { return path;
  }
  
  /**
   * 
   * @return The index of this ElementState within its parent ElementState
   */
  public int getIndex()
  { return path[path.length-1];
  }
  
  @Override
  public State getParent()
  { return parent;
  }
  
  @Override
  public State getChild(int index)
  { 
    if (children==null)
    { throw new IndexOutOfBoundsException("This ElementState has no children");
    }
    else
    { return children[index];
    }
  }
  
  /**
   * Resolve any references to meaningful ancestor states, for example, to
   *   propogate data contained in received messages.
   */
  public void resolve()
  { }
    
  
  @Override
  public void setChild(int index,State child)
  { 
    children[index]=child;
    if (child!=null)
    { 

      if (path==null)
      { path=new int[0];
      }
      
      int[] childPath=new int[path.length+1];
      System.arraycopy(path,0,childPath,0,path.length);
      childPath[childPath.length-1]=index;
      child.link(this,childPath);
    }
  }
  
  public final boolean frameChanged(StateFrame frame)
  {
    if (currentFrame!=frame)
    { 
      currentFrame=frame;
      return true;
    }
    return false;
  }
  
  /**
   * Find an ElementState among this ElementState's ancestors/containers
   * 
   * @param <X>
   * @param clazz
   * @return The ElementState with the specific class or interface, or null if
   *   none was found
   */
  @SuppressWarnings("unchecked") // Downcast from runtime check
  @Override
  public <X> X findState(Class<X> clazz)
  {
    if (clazz.isAssignableFrom(getClass()))
    { return (X) this;
    }
    else if (parent!=null)
    { return parent.<X>findState(clazz);
    }
    else
    { return null;
    }
  }
  
  /**
   * <p>Return an ancestor of this state that is the specified
   *   number of parents away.
   * </p>
   * 
   * @param distance The number of states to traverse, where 0 indicates
   *   that this state should be returned and 1 indicates that this state's
   *   parent should be returned.
   * @return The ancestor state.
   */
  @Override
  public State getAncestor(int distance)
  { 
    if (distance==0)
    { return this;
    }
    else if (parent!=null)
    { return parent.getAncestor(distance-1);
    }
    else
    { return null;
    }
  }
  
  @Override
  public String getComponentId()
  { return null;
  }
    
}


