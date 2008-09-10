//
// Copyright (c) 1998,2008 Michael Toth
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
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Logger;


import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLogger;
import spiralcraft.text.ParseException;
import spiralcraft.textgen.compiler.DocletUnit;
import spiralcraft.vfs.Resource;

/**
 * <p>Generates text from tgl markup contained in a Resource.
 *   Automatically updates when the markup changes.
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
class Generator<T extends DocletUnit>
  extends ResourceUnit<T>
{
  private static final Logger log
    =ClassLogger.getInstance(Generator.class);
  
  private final Focus<?> focus;
  private Element element;
  
  public Generator(Resource resource,Focus<?> focus)
  { 
    super(resource);
    this.focus=focus;
  }
  
  public Resource getResource()
  { return resource;
  }

  
  @Override
  protected void recompile()
  {
    super.recompile();
    if (unit!=null && exception==null)
    {
      try
      { 
        element=unit.bind(focus);
        exception=null;
      }
      catch (ParseException x)
      { 
        element=null;
        exception=x;
      }
    }
  }
  
  /**
   * <p>Render markup to a Writer, after checking for a resource update
   * </p>
   * 
   * @param writer
   * @throws IOException
   */
  public void render(Writer writer)
    throws IOException
  {
    checkState();
    if (exception==null)
    {
      EventContext context=new EventContext(writer,false);
      element.render(context);
    }
    else
    { log.warning("Caught exception rendering "+resource.getURI());
    }
    
  }
  
  /**
   * <p>Render markup to a String, after checking for a resource update
   * </p>
   * 
   * @return The rendered markup
   * @throws IOException
   */
  public String render()
    throws IOException
  {
    checkState();
    if (exception==null)
    {
      StringWriter writer=new StringWriter();
      EventContext context=new EventContext(writer,false);
      element.render(context);
      return writer.toString();
    }
    else
    { 
      log.warning("Caught exception rendering "+resource.getURI());
      return null;
    }
  }

  
}