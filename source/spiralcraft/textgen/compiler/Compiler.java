package spiralcraft.textgen.compiler;

import spiralcraft.text.Trimmer;

import spiralcraft.text.markup.MarkupCompiler;
import spiralcraft.text.markup.CompilationUnit;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.SyntaxException;

/**
 * Compiles a CharSequence containing Text Generation Markup Language
 *   into a tree of Units that can later be bound to an application
 *   context.
 */
public class Compiler
  extends MarkupCompiler
{
  
  private final Trimmer _trimmer=new Trimmer("\r\n\t ");
  
  public Compiler()                
  { super("<%","%>");
  }

  public CompilationUnit createCompilationUnit()
  { return new TglCompilationUnit();
  }
  
  public void handleMarkup(CharSequence code)
    throws Exception
  { 
    code=_trimmer.trim(code);
    if (code.charAt(0)=='/')
    { 
      // End tag case
      String unitName=code.subSequence(1,code.length()).toString();
      String expectName=getUnit().getName();
      if (unitName.equals(expectName))
      { closeUnit();
      }
      else
      { 
        if (expectName!=null)
        { 
          throw new SyntaxException
            ("Mismatched end tag. Found <%/"+unitName+"%>"
            +", expecting <%/"+expectName+"%>"
            );
        }
        else
        {
          throw new SyntaxException
            ("Unexpected end tag <%/"+unitName+"%>- no tags are open");
        }
      }
    }
    else
    {
      ElementUnit elementUnit=new ElementUnit(code);
      addUnit(elementUnit);
    }
  }
}
