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
package spiralcraft.textgen;

import java.util.ArrayList;
import java.util.Iterator;

public class IterationState
    extends ElementState
    implements Iterable<MementoState>
{
  
  private final int grandchildCount;
  
  private ArrayList<MementoState> children
    =new ArrayList<MementoState>();
  
  private volatile StateFrame lastFrame;
  
  public IterationState(int grandchildCount)
  { this.grandchildCount=grandchildCount;
  }

  public boolean frameChanged(StateFrame frame)
  {
    if (lastFrame!=frame)
    { 
      lastFrame=frame;
      return true;
    }
    return false;
  }
  
  public ElementState ensureChild(int index,Object memento)
  { 
    while (children.size()<=index)
    { 
      MementoState child=new MementoState(grandchildCount);
        
      int[] path=getPath();
      int[] childPath=new int[path.length+1];
      System.arraycopy(path,0,childPath,0,path.length);
      childPath[childPath.length-1]=index;
      child.setPath(childPath);      

      child.setParent(this);
        
      child.resolve();
      
      children.add(child);
    }

    MementoState child=children.get(index);
    child.setValue(memento);
    return child;
  }
  
  @Override
  public ElementState getChild(int index)
  { return children.size()>index?children.get(index):null;
  }
  
  public int getChildCount()
  { return children.size();
  }
  
  @Override
  public Iterator<MementoState> iterator()
  { return children.iterator();
  }
  
  public void trim(int size)
  { 
    while (children.size()>size)
    { children.remove(children.size()-1);
    }
  }

}



