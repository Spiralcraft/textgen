package spiralcraft.textgen.test;

import spiralcraft.stream.StreamUtil;
import spiralcraft.stream.url.URLResource;

import spiralcraft.textgen.Generator;

import spiralcraft.lang.BeanFocus;
import spiralcraft.lang.DefaultFocus;

import spiralcraft.tuple.Scheme;
import spiralcraft.tuple.Field;
import spiralcraft.tuple.Tuple;

import spiralcraft.tuple.lang.TupleBinding;

import spiralcraft.tuple.builder.AssemblyScheme;

import spiralcraft.tuple.spi.ArrayTuple;


import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import java.net.URL;

import java.util.Dictionary;
import java.util.Iterator;

import spiralcraft.time.Clock;

public class TupleTest
{
  public static void main(String[] args)
    throws Exception
  {
    
    URL assemblyURL
      =GeneratorTest.class.getResource("tupleTest.scheme.assembly.xml");
    
    InputStream in
      =GeneratorTest.class.getResourceAsStream("tupleTest.textgen");

    String content=new String(StreamUtil.readBytes(in));
    in.close();

    

    Scheme scheme=new AssemblyScheme(new URLResource(assemblyURL));
    
    Tuple tuple=new ArrayTuple(scheme);
    
    TupleBinding binding=new TupleBinding(scheme);
    binding.set(tuple);
    
    Dictionary dict=System.getProperties();
    Iterator it=scheme.getFields().iterator();
    while (it.hasNext())
    { 
      Field field=(Field) it.next();
      tuple.set(field,dict.get(field.getName().replace('_','.')));
    }

    Writer writer=new OutputStreamWriter(System.out);
    
    Generator generator=new Generator(content);
    generator.bind(new DefaultFocus(binding));
    generator.write(writer);

    writer.flush();
  }
    
}
