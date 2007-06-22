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

import spiralcraft.vfs.StreamUtil;

import spiralcraft.text.markup.MarkupParser;

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
      =ParserTest.class.getResourceAsStream("bigSyntaxTest.tgl");
    
    String content=new String(StreamUtil.readBytes(in));
    in.close();

    MarkupParser parser=new MarkupParser("<%","%>");

    StubHandler handler=new StubHandler();
    parser.setMarkupHandler(handler);

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
