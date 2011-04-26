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
import java.net.URI;


import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;

import spiralcraft.text.Renderer;

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
public class Generator
  extends ResourceUnit<DocletUnit>
  implements Renderer
{
  private static final ClassLog log
    =ClassLog.getInstance(Generator.class);
  
  private final Focus<?> focus;
  private Element element;
  
  /**
   * <p>Create a generator using the markup at the specifed resource bound
   *   to the specified Focus chain.
   * </p>
   * 
   * @param resource
   * @param focus
   */
  public Generator(Resource resource,Focus<?> focus)
  { 
    super(resource);
    this.focus=focus;
  }
  
  /**
   * <p>Create a generator using the markup at the specifed URI bound
   *   to the specified Focus chain.
   * </p>
   * 
   * @param resource
   * @param focus
   */
  public Generator(URI uri,Focus<?> focus)
    throws IOException
  { 
    super(uri);
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
        element=unit.bind(focus,null);
        exception=null;
      }
      catch (ContextualException x)
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
  @Override
  public void render(Appendable writer)
    throws IOException
  {
    checkState();
    if (exception==null)
    {
      if (element!=null)
      {
        EventContext context=new EventContext(writer,false,null);
        context.dispatch(RenderMessage.INSTANCE,element,null);
      }
      else
      {
        if (!resource.exists())
        { 
          log.warning("Resource does not exist "+resource.getURI());
        }
        else
        { 
          log.warning
            ("Unable to retrieve element from resource "+resource.getURI());
        }
      }
    }
    else
    { 
      log.warning("Caught exception rendering "+resource.getURI());
      exception.printStackTrace();
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
    StringWriter writer=new StringWriter();
    render(writer);
    return writer.toString();
  }


  
}