package spiralcraft.textgen.compiler;

import java.io.Writer;
import java.io.IOException;

import spiralcraft.textgen.ParseException;
import spiralcraft.textgen.Element;

import spiralcraft.builder.Assembly;

/**
 * A Unit which contains literal text
 */
public class TextUnit
  extends Unit
{
  private CharSequence _text;
  
  public TextUnit(CharSequence text)
  { _text=text;
  }
  
  public String toString()
  { return super.toString()+"[text]";
  }
  
  public Element bind(Assembly parent,Element parentElement)
  { return new TextElement();
  }
  
  class TextElement
    extends Element
  {
    public void write(Writer writer)
      throws IOException
    { 
      int len=_text.length();
      for (int i=0;i<len;i++)
      { writer.write(_text.charAt(i));
      }
    }
  }
}
