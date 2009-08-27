//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.textgen.test;


import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;

import spiralcraft.textgen.compiler.TglCompiler;
import spiralcraft.textgen.compiler.DocletUnit;

import spiralcraft.lang.reflect.BeanFocus;

import java.io.OutputStreamWriter;
//import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;

//import spiralcraft.time.Clock;

import spiralcraft.data.persist.XmlAssembly;
import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionContext;
import spiralcraft.exec.ExecutionException;

/**
 * Parser for text generation markup language.
 */
public class GeneratorTest
  implements Executable
{
  @SuppressWarnings("unchecked") // Heterogeneous use of lang package
  public void execute(String ... args)
    throws ExecutionException
  {
    ExecutionContext exContext=
      ExecutionContext.getInstance();
    
    try
    {

    URI uri=URI.create("class:/spiralcraft/textgen/test/generatorTest.tgl");
    DocletUnit root
      =new TglCompiler().compile(uri);
    

    Element element
      =root.bind
        (new BeanFocus
          (new XmlAssembly
              (URI.create("class:/spiralcraft/textgen/test/MyWidget.assy"),null)
          .get()
          )
        );

    Writer writer=new OutputStreamWriter(exContext.out());
    EventContext context=new EventContext(writer,false,null);
    element.render(context);
    writer.flush();

//    if (false)
//    {
//      Clock clock=Clock.instance();
//      long time=System.currentTimeMillis();
//      long duration=5000;
//
//      StringWriter stringWriter=new StringWriter();
//      context=new EventContext(writer,false,null);
//      long iterations=0;
//      while (true)
//      { 
//        element.render(context);
//        stringWriter.getBuffer().setLength(0);
//        iterations++;
//        if (clock.approxTimeMillis()-time>duration)
//        { break;
//        }
//      }
//
//      exContext.out().println
//        ("rate="+(iterations) / ((System.currentTimeMillis()-time)/(float) 1000));
//    }
    }
    catch (Exception x)
    { throw new ExecutionException("Error",x);
    }
  
  }
  
}
