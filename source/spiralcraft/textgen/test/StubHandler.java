package spiralcraft.textgen.test;

import spiralcraft.textgen.compiler.ContentHandler;

import java.io.PrintWriter;

public class StubHandler
  implements ContentHandler
{ 
  private PrintWriter _debugWriter;

  public void setDebugWriter(PrintWriter writer)
  { _debugWriter=writer;
  }
  
  public void handleText(CharSequence text)
  {
    if (_debugWriter!=null)
    {
      _debugWriter.println("TEXT:");
      _debugWriter.println(text);
      _debugWriter.println("/TEXT");
    }
  }
  
  public void handleCode(CharSequence code)
  {
    if (_debugWriter!=null)
    {
      _debugWriter.println("CODE:");
      _debugWriter.println(code);
      _debugWriter.println("/CODE");
    }
  }
}
