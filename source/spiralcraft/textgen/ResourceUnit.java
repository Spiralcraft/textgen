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

import spiralcraft.app.Component;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.text.ParseException;

import spiralcraft.textgen.compiler.DocletUnit;
import spiralcraft.textgen.compiler.TglCompiler;

import spiralcraft.vfs.Resource;
import spiralcraft.vfs.Resolver;

import java.net.URI;

import spiralcraft.time.Clock;

/**
 * <p>Compiles a TglCompilationUnit from a Resource for use as a binding
 * factory.
 * </p>
 * 
 * <p>Checks the lastModified time of the Resource at a specified frequency
 * and automatically recompiles if necessary.
 * </p>
 * 
 * @author mike
 *
 */
public class ResourceUnit<T extends DocletUnit>
{
  private static final ClassLog log
    =ClassLog.getInstance(ResourceUnit.class);
  private Level logLevel
    =ClassLog.getInitialDebugLevel(ResourceUnit.class,null);
  
  private final TglCompiler<T> compiler;
  protected final Resource resource;
  protected T unit;
  protected Exception exception;
  
  private long lastRead;
  private long lastChecked;
  private long lastRecompile;
  private int checkFrequencyMs=-1;
  private int maxRecompileRateMs=500;


  public ResourceUnit(URI uri)
    throws IOException
  { 
    
    this.compiler=createCompiler();
    this.resource=Resolver.getInstance().resolve(uri);
  }

  public ResourceUnit(Resource resource)
  {
    this.compiler=createCompiler();
    this.resource=resource;
  }
  
  /**
   * Create the compiler. Override to use an extended implementation
   *   of the compiler.
   * 
   * @return A new TglCompiler instance
   */
  protected TglCompiler<T> createCompiler()
  { return new TglCompiler<T>();
  }
  
  public ResourceUnit(URI uri,TglCompiler<T> compiler)
    throws IOException
  {
    this.compiler=compiler;
    this.resource=Resolver.getInstance().resolve(uri);
  }
  
  public ResourceUnit(Resource resource,TglCompiler<T> compiler)
  {
    this.compiler=compiler;
    this.resource=resource;
  }

  /**
   * 
   * @return Whether the resource exists
   * @throws IOException
   */
  public boolean exists()
    throws IOException
  { return resource.exists();
  }
  
  /**
   * <p>Specify the interval in milliseconds to check whether the resource
   *   has been modified. Turned off by default (-1).
   * </p>
   *   
   * @param frequencyMs The interval in milliseconds, or -1 to turn off
   *   automatic recompilation. 
   */
  public void setCheckFrequencyMs(int frequencyMs)
  { this.checkFrequencyMs=frequencyMs;
  }
  
  /**
   * 
   * @return The Unit compiled from the Resource, or null if there was
   *   a compilation exception.
   */
  public DocletUnit getUnit()
  { 
    if (lastChecked==0 || checkFrequencyMs>-1)
    { checkState();
    }
    return unit;
  }
  
  /**
   * 
   * @return Any exception that occurred during compilation, or null if
   *   compilation was successful
   */
  public Exception getException()
  { return exception;
  }
  
  public synchronized void checkState()
  {
    long now=Clock.instance().approxTimeMillis();
    
    // Ensure that checking does not happen too often
    if (lastChecked!=0 && now-lastChecked<checkFrequencyMs)
    { return;
    }
    
    lastChecked=now;
    
    try
    {
      long lastModified
        =unit!=null?unit.getLastModified():resource.getLastModified();
      
      if (lastModified>lastRead
          || (exception!=null
              && now-maxRecompileRateMs>lastRecompile
             )
          || (lastModified==0 && unit==null && exception==null)
          || hasStaleElements()
         )
      { 
        if (logLevel.isDebug())
        { log.debug("lastRead="+lastRead+"  lastModified="+lastModified);
        }
        recompile();
        lastRead=lastModified;
        if (unit!=null)
        { lastRead=unit.getLastModified();
        }
      }
      
      
    }
    catch (IOException x)
    { 
      unit=null;
      exception=x;
    }
  }
  
  private boolean hasStaleElements()
  { return unit!=null && unit.hasStaleElements();
  }

  public Component bind(Focus<?> focus)
    throws ContextualException,IOException
  {
    DocletUnit unit=getUnit();
    if (unit!=null)
    { return unit.bind(focus,null);
    }
    else
    {
      if (exception instanceof IOException)
      { throw (IOException) exception;
      }
      else if (exception instanceof ParseException)
      { throw (ParseException) exception;
      }
      else if (exception!=null)
      { throw new RuntimeException(exception.toString(),exception);
      }
      else
      { throw new IOException
          (resource.getURI()+" not compiled- last modified "
          +resource.getLastModified()
          );
      }
    }
  }
  
  protected void recompile()
  {
    try
    { 
      if (logLevel.isDebug())
      { log.debug("Compiling "+resource);
      }
      unit=compiler.compile(resource.getURI());
      exception=null;
    }
    catch (IOException x)
    { 
      unit=null;
      exception=x;
    }
    catch (ParseException x)
    { 
      unit=null;
      exception=x;
    }
    lastRecompile=Clock.instance().approxTimeMillis();
  }
}
