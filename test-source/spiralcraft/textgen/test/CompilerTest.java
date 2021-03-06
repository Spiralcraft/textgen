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

import spiralcraft.textgen.compiler.TglCompiler;
import spiralcraft.textgen.compiler.DocletUnit;

import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionContext;
import spiralcraft.exec.ExecutionException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanFocus;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import spiralcraft.time.Clock;

import java.net.URI;

/**
 * Parser for text generation markup language.
 */
public class CompilerTest
  implements Executable
{
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" }) // Heterogeneous use of lang package
  public void execute(String ... args)
    throws ExecutionException
  {
    ExecutionContext context=
      ExecutionContext.getInstance();
    
    try
    {
      URI uri=URI.create("class:/spiralcraft/textgen/test/generatorTest.tgl");
      TglCompiler compiler
      =new TglCompiler();
      DocletUnit root=compiler.compile(uri);
      root.dumpTree(new PrintWriter(new OutputStreamWriter(context.out())),"| ");

      Clock clock=Clock.instance();
      long time=System.currentTimeMillis();
      long duration=60000;

      int iterations=0;
      Focus focus=new BeanFocus(new CompilerTest());
      while (true)
      { 
        DocletUnit unit=compiler.compile(uri);
        unit.bind(focus,null);
        iterations++;
        if (clock.approxTimeMillis()-time>duration)
        { break;
        }
      }
      context.out().println
        ("rate="
        +(iterations) / ((System.currentTimeMillis()-time)/(float) 1000)
        );
    }
    catch (Exception x)
    { throw new ExecutionException("Error",x);
    }
  }
  
}
