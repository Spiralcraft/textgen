package spiralcraft.textgen;

import java.util.LinkedList;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.BuildException;

import spiralcraft.lang.BindException;

/**
 * A Unit of text generation which represents a
 *   node in the tree structure of a TextGenML document.
 */
public abstract class Unit
{
  private LinkedList _children;
  private Unit _parent;
  private String _name;
  
  public void addChild(Unit child)
  { 
    if (_children==null)
    { _children=new LinkedList();
    }
    _children.add(child);
    child.setParent(this);
  }
  
  public void setParent(Unit parent)
  { _parent=parent;
  }
  
  public Unit getParent()
  { return _parent;
  }
  
  public String getName()
  { return _name;
  }
  
  protected void setName(String name)
  { _name=name;
  }

  public Unit[] getChildren()
  { 
    if (_children==null)
    { return new Unit[0];
    }
    Unit[] children=new Unit[_children.size()];
    _children.toArray(children);
    return children;
  }  
  
  public void close()
    throws ParseException
  {
  }
  
  /**
   * Create a tree of Tags bound into an application context (the Assembly)
   *   which implements the functional behavior specified by the TextGenML 
   *   document.
   */
  public abstract Tag bind(Assembly parent,Tag parentTag)
    throws BuildException,BindException;

  protected void bindChildren(Assembly assembly,Tag tag)
    throws BuildException,BindException
  {
    Unit[] children=getChildren();
    if (children.length>0)
    { 
      Tag[] childTags=new Tag[children.length];
      for (int i=0;i<children.length;i++)
      { childTags[i]=children[i].bind(assembly,tag);
      }
      tag.setChildren(childTags);
    }
  }
    

  
}
