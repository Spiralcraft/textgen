package spiralcraft.textgen;

import java.io.Writer;
import java.io.IOException;

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
  
  public Tag bind(Assembly parent,Tag parentTag)
  { return new TextTag();
  }
  
  class TextTag
    extends Tag
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
