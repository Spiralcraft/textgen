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
package spiralcraft.textgen;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import spiralcraft.text.io.ResourceCharSequence;

import spiralcraft.textgen.compiler.TglCompiler;
import spiralcraft.textgen.compiler.TglCompilationUnit;

import spiralcraft.text.ParseException;
import spiralcraft.text.markup.MarkupException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;


import java.net.URI;

/**
 * Generates textual output from application data exposed by the
 *   spiralcraft.lang expression language.
 *
 * Represents a compiled unit of textgen source bound to a 
 *   spiralcraft.data Focus.
 */
public class Generator
{

  private final TglCompilationUnit root;
  private final URI sourceURI;
  
  /**
   * Construct a Generator from the given source
   */
  public Generator(URI sourceURI)
    throws IOException,ParseException,MarkupException
  {  
    this.sourceURI=sourceURI; 
    CharSequence sequence = new ResourceCharSequence(sourceURI);
    TglCompiler tglCompiler=new TglCompiler();
    root=new TglCompilationUnit(sourceURI);
    tglCompiler.compile(root,sequence);
  }
  
  public URI getSourceURI()
  { return sourceURI;
  }
  
  /**
   * Bind the Generator to an application Focus.
   */
  public Element bind(Focus<?> focus)
    throws BindException
  { 
    Element element=root.bind(focus);
    return element;
  }
  

  public GenerationContext createGenerationContext(Writer writer)
  { return new GenerationContext(writer);
  }
  
  /**
   * Output debugging information about the structure of the source
   */
  public void debug(PrintWriter writer)
  { root.dumpTree(writer,"");
  }
  

  
}
