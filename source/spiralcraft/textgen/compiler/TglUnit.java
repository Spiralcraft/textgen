package spiralcraft.textgen.compiler;

import java.util.LinkedList;

import spiralcraft.textgen.MarkupException;
import spiralcraft.textgen.Element;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.BuildException;

import spiralcraft.lang.BindException;

/**
 * A Unit of text generation which represents a
 *   node in the tree structure of a TGL block.
 */
public interface TglUnit
{
  
  /**
   * Create a tree of Elements bound into an application context (the Assembly)
   *   which implements the functional behavior specified by the TGL 
   *   document.
   */
  public Element bind(Assembly parent,Element parentElement)
    throws BuildException,BindException;

  
}
