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
