package spiralcraft.textgen.test;

import spiralcraft.stream.StreamUtil;

import spiralcraft.textgen.Compiler;
import spiralcraft.textgen.Unit;
import spiralcraft.textgen.RootUnit;
import spiralcraft.textgen.Tag;

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

    Compiler compiler=new Compiler();
    Unit unit=compiler.compile(content);
    dump(unit,"");

    Clock clock=Clock.instance();
    long time=System.currentTimeMillis();
    long duration=60000;
    
    int iterations=0;
    Focus focus=new BeanFocus(new CompilerTest());
    while (true)
    { 
      RootUnit root=compiler.compile(content);
      Tag tag=root.bind(null,focus);
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
