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

import spiralcraft.util.Sequence;

public class IterationState
    extends ElementState
    implements Iterable<MementoState>
{
  
  private final int grandchildCount;
  
  private ArrayList<MementoState> children
    =new ArrayList<MementoState>();
  
  public IterationState(int grandchildCount)
  { this.grandchildCount=grandchildCount;
  }


  
  public ElementState ensureChild(int index,Object memento)
  { 
    while (children.size()<=index)
    { 
      MementoState child=new MementoState(grandchildCount);
        
      Sequence<Integer> path=getPath();
      Sequence<Integer> childPath
        =path.concat(new Integer[] {index});
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



