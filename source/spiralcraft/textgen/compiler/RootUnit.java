package spiralcraft.textgen.compiler;

import spiralcraft.textgen.ParseException;
import spiralcraft.textgen.Tag;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.BuildException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;

import java.io.Writer;
import java.io.IOException;

/**
 * The root of a TGL compilation unit
 */
public class RootUnit
  extends Unit
{
  public RootTag bind(Assembly parent,Focus focus)
    throws BuildException,BindException
  { 
    RootTag tag=new RootTag();
    tag.setFocus(focus);
    bindChildren(parent,tag);
    return tag;
  }
  
  public Tag bind(Assembly parent,Tag parentTag)
    throws BuildException,BindException
  { 
    Tag tag=new RootTag();
    tag.bind(parentTag);
    bindChildren(parent,tag);
    return tag;
  }
  
  
  public String toString()
  { return super.toString()+"[root]";
  }  

  class RootTag
    extends Tag
  {
    private Focus _focus;
    
    public void setFocus(Focus focus)
    { _focus=focus;
    }
    
    public Focus getFocus()
    { 
      if (_focus!=null)
      { return _focus;
      }
      return super.getFocus();
    }
    
    public void write(Writer out)
      throws IOException
    { writeChildren(out);
    }
  }
}
