package spiralcraft.textgen.test;

import spiralcraft.stream.StreamUtil;

import spiralcraft.textgen.Generator;

import spiralcraft.lang.Focus;
import spiralcraft.lang.BeanFocus;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import spiralcraft.time.Clock;

/**
 * Parser for text generation markup language.
 */
public class CompilerTest
{
  public static void main(String[] args)
    throws Exception
  {
    InputStream in
      =ParserTest.class.getResourceAsStream("generatorTest.textgen");
    
    String content=new String(StreamUtil.readBytes(in));
    in.close();

    Generator generator=new Generator(content);
    generator.debug(new PrintWriter(new OutputStreamWriter(System.out)));

    Clock clock=Clock.instance();
    long time=System.currentTimeMillis();
    long duration=60000;
    
    int iterations=0;
    Focus focus=new BeanFocus(new CompilerTest());
    while (true)
    { 
      generator=new Generator(content);
      generator.bind(focus);
      iterations++;
      if (clock.approxTimeMillis()-time>duration)
      { break;
      }
    }
    System.out.println("rate="+(iterations) / ((System.currentTimeMillis()-time)/(float) 1000));
    
  }
  
}
