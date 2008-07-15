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
package spiralcraft.textgen.compiler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import spiralcraft.text.ParseException;
import spiralcraft.text.markup.MarkupException;

public abstract class ProcessingUnit
  extends TglUnit
{

  protected boolean open=true;
  
  public ProcessingUnit(TglUnit parent)
  { super(parent);
  }

  public boolean isOpen()
  { return open;
  }
  
  public void close()
  { open=false;
  }
  
  protected DocletUnit includeResource(String resourceRef,TglCompiler<?> compiler)
    throws MarkupException
  {
    URI resourceURI=null;
    try
    { resourceURI=new URI(resourceRef);
    }
    catch (URISyntaxException x)
    { 
      throw new MarkupException
      ("Error creating URI '"+resourceRef+"':"+x
          ,compiler.getPosition()
      );
    }


    if (!resourceURI.isAbsolute())
    {
      DocletUnit parentDoc=findUnit(DocletUnit.class);
      URI baseURI=parentDoc.getSourceURI();
      resourceURI=baseURI.resolve(resourceURI);

    }
    try
    { 
      // This will add the Unit defined by the specified resource
      //   as the first child of this unit.
      return compiler.subCompile(this,resourceURI);
    }
    catch (ParseException x)
    { 

      throw new MarkupException
      ("Error including URI '"+resourceRef+"':"+x
          ,compiler.getPosition()
          ,x
      );
    }
    catch (IOException x)
    {
      throw new MarkupException
      ("Error including URI '"+resourceRef+"':"+x
          ,compiler.getPosition()
          ,x
      );
    }
  }
  
}
