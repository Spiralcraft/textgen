package spiralcraft.textgen.test;

import spiralcraft.stream.StreamUtil;

import spiralcraft.textgen.Compiler;
import spiralcraft.textgen.Unit;
import spiralcraft.textgen.RootUnit;
import spiralcraft.textgen.Tag;

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

    Compiler compiler=new Compiler();
    RootUnit unit=compiler.compile(content);
    Tag tag=unit.bind(null,new BeanFocus(new GeneratorTest()));
    
    Writer writer=new OutputStreamWriter(System.out);
    tag.write(writer);
    writer.flush();

    Clock clock=Clock.instance();
    long time=System.currentTimeMillis();
    long duration=5000;

    StringWriter stringWriter=new StringWriter();

    long iterations=0;
    while (true)
    { 
      tag.write(stringWriter);
      stringWriter.getBuffer().setLength(0);
      iterations++;
      if (clock.approxTimeMillis()-time>duration)
      { break;
      }
    }
   
    System.out.println("rate="+(iterations) / ((System.currentTimeMillis()-time)/(float) 1000));
    
  }
  
  public static void dump(Unit unit,String linePrefix)
  {
    System.out.println(linePrefix+unit.toString());
    Unit[] children=unit.getChildren();
    for (int i=0;i<children.length;i++)
    { dump(children[i],linePrefix+"  ");
    }
  }  
}
