package spiralcraft.textgen.compiler;

import java.io.Writer;
import java.io.IOException;

import spiralcraft.textgen.Element;

import spiralcraft.builder.Assembly;

import spiralcraft.text.markup.ContentUnit;

/**
 * A Unit which contains literal text
 */
public class TglContentUnit
  extends ContentUnit
  implements TglUnit
{
  
  public TglContentUnit(CharSequence content)
  { super(content);
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
      CharSequence content=getContent();
      int len=content.length();
      for (int i=0;i<len;i++)
      { writer.write(content.charAt(i));
      }
    }
  }
}
