package spiralcraft.textgen;

import java.io.IOException;
import java.io.Writer;
import java.io.PrintWriter;

import spiralcraft.textgen.compiler.Compiler;
import spiralcraft.textgen.compiler.CompilationUnit;
import spiralcraft.textgen.compiler.Unit;

import spiralcraft.text.ParseException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;

import spiralcraft.builder.BuildException;

/**
 * Generates textual output from application data. 
 *
 * Represents a compiled unit of textgen source bound to a data Focus.
 */
public class Generator
{

  private final CompilationUnit _root;
  private Element _element;

  /**
   * Construct a Generator from the given source
   */
  public Generator(CharSequence source)
    throws GeneratorException
  {  
    Compiler compiler=new Compiler();
    try
    { _root=compiler.compile(source);
    }
    catch (ParseException x)
    { throw new GeneratorException(x);
    }
  }
  
  /**
   * Bind the Generator to an application Focus.
   */
  public void bind(Focus focus)
    throws GeneratorException
  { 
    try
    { _element=_root.bind(null,focus);
    }
    catch (BindException x)
    { throw new GeneratorException(x);
    }
    catch (BuildException x)
    { throw new GeneratorException(x);
    }
  }
  
  public void write(Writer writer)
    throws IOException
  { _element.write(writer);
  }

  /**
   * Output debugging information about the structure of the source
   */
  public void debug(PrintWriter writer)
  { debug(writer,_root,"");
  }
  
  private void debug(PrintWriter writer,Unit unit,String linePrefix)
  {
    writer.println(linePrefix+unit.toString());
    Unit[] children=unit.getChildren();
    for (int i=0;i<children.length;i++)
    { debug(writer,children[i],linePrefix+"  ");
    }
  }  
  
}
