package spiralcraft.textgen;

public interface ContentHandler
{
  void handleText(CharSequence text);
  
  void handleCode(CharSequence code)
    throws ParseException;
}
