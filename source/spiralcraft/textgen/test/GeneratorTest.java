package spiralcraft.textgen.test;

import spiralcraft.stream.StreamUtil;

import spiralcraft.textgen.Generator;

import spiralcraft.lang.BeanFocus;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import spiralcraft.time.Clock;

/**
 * Parser for text generation markup language.
 */
public class GeneratorTest
{
  public static void main(String[] args)
    throws Exception
  {
    InputStream in
      =GeneratorTest.class.getResourceAsStream("generatorTest.textgen");
    
    String content=new String(StreamUtil.readBytes(in));
    in.close();

    Generator generator=new Generator(content);
    generator.bind(new BeanFocus(new GeneratorTest()));
    
    Writer writer=new OutputStreamWriter(System.out);
    generator.write(writer);
    writer.flush();

    Clock clock=Clock.instance();
    long time=System.currentTimeMillis();
    long duration=5000;

    StringWriter stringWriter=new StringWriter();

    long iterations=0;
    while (true)
    { 
      generator.write(stringWriter);
      stringWriter.getBuffer().setLength(0);
      iterations++;
      if (clock.approxTimeMillis()-time>duration)
      { break;
      }
    }
   
    System.out.println("rate="+(iterations) / ((System.currentTimeMillis()-time)/(float) 1000));
    
  }
  
}
