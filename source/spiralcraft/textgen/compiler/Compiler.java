package spiralcraft.textgen.compiler;

import spiralcraft.text.Trimmer;

import spiralcraft.textgen.ParseException;
import spiralcraft.textgen.Element;

/**
 * Compiles a CharSequence containing Text Generation Markup Language
 *   into a tree of Units that can later be bound to an application
 *   context.
 */
public class Compiler
  implements ContentHandler
{
  
  private final Parser _parser;
  private final Trimmer _trimmer=new Trimmer("\r\n\t ");
  private Unit _unit;
  
  public Compiler()
  { 
    _parser=new Parser();
    _parser.setContentHandler(this);
  }
  
  public synchronized RootUnit compile(CharSequence sequence)
    throws ParseException
  { 
    _unit=new RootUnit();
    _parser.parse(sequence);
    if (!(_unit instanceof RootUnit))
    { throw new ParseException("Unexpected end of input. Expected <%/"+_unit.getName()+"%>");
    }
    return (RootUnit)  _unit;
  }

  public void handleText(CharSequence text)
  { _unit.addChild(new TextUnit(text));
  }
  
  public void handleCode(CharSequence code)
    throws ParseException
  { 
    code=_trimmer.trim(code);
    if (code.charAt(0)=='/')
    { 
      // End tag case
      String unitName=code.subSequence(1,code.length()).toString();
      String expectName=_unit.getName();
      if (unitName.equals(expectName))
      { 
        _unit.close();
        _unit=_unit.getParent();
      }
      else
      { 
        if (expectName!=null)
        { 
          throw new ParseException
            ("Mismatched end tag. Found <%/"+unitName+"%>"
            +", expecting <%/"+expectName+"%>"
            );
        }
        else
        {
          throw new ParseException
            ("Unexpected end tag <%/"+unitName+"%>- no tags are open");
        }
      }
    }
    else
    {
      ElementUnit elementUnit=new ElementUnit(code);
      _unit.addChild(elementUnit);
      if (elementUnit.isOpen())
      { _unit=elementUnit;
      }
    }
  }
}
