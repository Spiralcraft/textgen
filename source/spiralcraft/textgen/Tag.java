package spiralcraft.textgen;

import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;

import java.io.Writer;
import java.io.IOException;

/**
 * A stateful processing unit in a TextGenML document,
 *   associated with a CodeUnit node in the
 *   document structure.
 *
 * A tree of Tags is generated each time the Unit tree
 *   in the document is bound to an application context.
 */
public abstract class Tag
{ 
  private Tag[] _children;
  private Tag _parent;
  
  /**
   * Called when binding Units
   */
  public void setChildren(Tag[] children)
  { _children=children;
  }

  public Tag[] getChildren()
  { return _children;
  }
  
  public Focus getFocus()
  { return _parent.getFocus();
  }
  
  /**
   * Called when binding Units. When this method is called,
   *   a Tag's ancestors will be visible but its children
   *   will not be.
   */
  public void bind(Tag parent)
    throws BindException
  { _parent=parent;
  }

  protected void writeChildren(Writer writer)
    throws IOException
  {
    if (_children!=null)
    { 
      for (int i=0;i<_children.length;i++)
      { _children[i].write(writer);
      }
    }
  }

  /**
   * Recursively perform processing and write output
   */
  public abstract void write(Writer writer)
    throws IOException;
}
