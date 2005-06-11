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

import spiralcraft.textgen.compiler.ContentHandler;

import java.io.PrintWriter;

public class StubHandler
  implements ContentHandler
{ 
  private PrintWriter _debugWriter;

  public void setDebugWriter(PrintWriter writer)
  { _debugWriter=writer;
  }
  
  public void handleText(CharSequence text)
  {
    if (_debugWriter!=null)
    {
      _debugWriter.println("TEXT:");
      _debugWriter.println(text);
      _debugWriter.println("/TEXT");
    }
  }
  
  public void handleCode(CharSequence code)
  {
    if (_debugWriter!=null)
    {
      _debugWriter.println("CODE:");
      _debugWriter.println(code);
      _debugWriter.println("/CODE");
    }
  }
}
