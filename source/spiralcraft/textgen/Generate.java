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

import java.io.IOException;
import java.net.URI;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.task.AbstractTask;
import spiralcraft.task.Scenario;
import spiralcraft.task.Task;

/**
 * <p>A scenario which generates output from a template and publishes the
 *   result to a location in the FocusChain
 * </p>
 * 
 * @author mike
 *
 */
public class Generate
    extends Scenario
{

  private Expression<String> targetX;
  private Channel<String> target;
  private URI templateURI;
  private Generator generator;
  
  /**
   * An expression which specifies where to store the generated content 
   * 
   * @param targetX
   */
  public void setTargetX(Expression<String> targetX)
  { this.targetX=targetX;
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
      { target.set(generator.render());
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
    else
    { throw new BindException("Property targetX is required");
    }
    
    try
    { generator=new Generator(templateURI,focus);
    }
    catch (IOException x)
    { throw new BindException("Error loading template "+templateURI,x);
    }
  }
    
}
