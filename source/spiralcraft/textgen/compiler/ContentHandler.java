package spiralcraft.textgen.compiler;

import spiralcraft.textgen.ParseException;

public interface ContentHandler
{
  void handleText(CharSequence text);
  
  void handleCode(CharSequence code)
    throws ParseException;
}
