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

import spiralcraft.stream.StreamUtil;

import spiralcraft.textgen.Generator;

import spiralcraft.lang.BeanFocus;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import spiralcraft.time.Clock;

import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionContext;
import spiralcraft.exec.ExecutionTargetException;

/**
 * Parser for text generation markup language.
 */
public class GeneratorTest
  implements Executable
{
  public void execute(ExecutionContext context,String[] args)
    throws ExecutionTargetException
  {
    try
    {
      InputStream in
        =GeneratorTest.class.getResourceAsStream("generatorTest.textgen");
      
      String content=new String(StreamUtil.readBytes(in));
      in.close();
  
      Generator generator=new Generator(content);
      generator.bind(new BeanFocus(new GeneratorTest()));
      
      Writer writer=new OutputStreamWriter(ExecutionContext.getInstance().out());
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
     
      context.out().println("rate="+(iterations) / ((System.currentTimeMillis()-time)/(float) 1000));
    }
    catch (Exception x)
    { throw new ExecutionTargetException(x);
    }
    
  }
  
}
