package spiralcraft.textgen.test;

import spiralcraft.stream.StreamUtil;

import spiralcraft.textgen.Parser;
import spiralcraft.textgen.ContentHandler;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import spiralcraft.time.Clock;

/**
 * Parser for text generation markup language.
 */
public class ParserTest
{
  public static void main(String[] args)
    throws Exception
  {
    InputStream in
      =ParserTest.class.getResourceAsStream("bigSyntaxTest.textgen");
    
    String content=new String(StreamUtil.readBytes(in));
    in.close();

    Parser parser=new Parser();

    StubHandler handler=new StubHandler();
    parser.setContentHandler(handler);

    handler.setDebugWriter(new PrintWriter(new OutputStreamWriter(System.out),true));    
    parser.parse(content);
    handler.setDebugWriter(null);

    Clock clock=Clock.instance();
    long time=System.currentTimeMillis();
    long duration=30000;
    
    int iterations=0;
    while (true)
    { 
      parser.parse(content);
      iterations++;
      if (clock.approxTimeMillis()-time>duration)
      { break;
      }
    }
    System.out.println("rate="+(iterations) / ((System.currentTimeMillis()-time)/(float) 1000));
    
  }
  
  
}
