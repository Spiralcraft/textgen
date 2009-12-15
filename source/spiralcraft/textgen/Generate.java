//
// Copyright (c) 2009 Michael Toth
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;

/**
 * <p>A scenario which generates output from a template and publishes the
 *   result to a location in the FocusChain
 * </p>
 * 
 * @author mike
 *
 */
public class Generate
    extends Scenario<Void,Void>
{

  private Expression<String> targetX;
  private Channel<String> target;
  private Expression<URI> outputUriX;
  private Channel<URI> outputUri;
  private URI templateURI;
  private Generator generator;
  
  /**
   * An expression which specifies where to store the generated content
   *   as a String
   * 
   * @param targetX
   */
  public void setTargetX(Expression<String> targetX)
  { this.targetX=targetX;
  }
  
  /**
   * An expression which specifies a destination URI for the generated content 
   * 
   * @param targetX
   */
  public void setOutputUriX(Expression<URI> outputUriX)
  { this.outputUriX=outputUriX;
  }
  
  /**
   * The template to use for generating the content
   * 
   * @param templateURI
   */
  public void setTemplateURI(URI templateURI)
  { this.templateURI=templateURI;
  }
  
  @Override
  public Task task()
  { return new GenerateTask();
  }
  
  public class GenerateTask
    extends AbstractTask
  {

    @Override
    protected void work() throws InterruptedException
    {
      try
      { 
        if (target!=null)
        { target.set(generator.render());
        }
        else
        { 
          Resource resource=Resolver.getInstance().resolve(outputUri.get());
          OutputStream out=resource.getOutputStream();
          Writer writer=new BufferedWriter(new OutputStreamWriter(out));
          try
          {
            generator.render(writer);
            writer.flush();
          }
          finally
          { writer.close();
          }
        }
      }
      catch (IOException x)
      { addException(x);
      }
      
    }
  }
  
  @Override
  public void bindChildren(Focus<?> focus)
    throws BindException
  {
    if (targetX!=null)
    { target=focus.bind(targetX);
    }
    else if (outputUriX!=null)
    { outputUri=focus.bind(outputUriX);
    }
    else
    { throw new BindException("Property targetX or outputUriX is required");
    }
    
    try
    { generator=new Generator(templateURI,focus);
    }
    catch (IOException x)
    { throw new BindException("Error loading template "+templateURI,x);
    }
  }
    
}
