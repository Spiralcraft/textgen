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

import spiralcraft.lang.Focus;
import spiralcraft.text.ParseException;

import spiralcraft.textgen.compiler.DocletUnit;
import spiralcraft.textgen.compiler.TglCompiler;

import spiralcraft.vfs.Resource;
import spiralcraft.vfs.Resolver;

import java.net.URI;

import spiralcraft.time.Clock;

/**
 * <P>Compiles a TglCompilationUnit from a Resource for use as a binding
 * factory.
 * </P>
 * 
 * <P>Checks the lastModified time of the Resource at a specified frequency
 * and automatically recompiles if necessary.
 * </P>
 * 
 * @author mike
 *
 */
public class ResourceUnit<T extends DocletUnit>
{
  private final TglCompiler<T> compiler;
  private final Resource resource;
  private T unit;
  private long lastRead;
  private long lastChecked;
  private long lastRecompile;
  private int checkFrequencyMs=-1;
  private int maxRecompileRateMs=500;

  private Exception exception;

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
   * Specify the interval in milliseconds to check whether the resource
   *   has been modified. 
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
         )
      { 
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

  public Element bind(Focus<?> focus)
    throws ParseException,IOException
  {
    DocletUnit unit=getUnit();
    if (unit!=null)
    { return unit.bind(focus);
    }
    else
    {
      if (exception instanceof IOException)
      { throw (IOException) exception;
      }
      else if (exception instanceof ParseException)
      { throw (ParseException) exception;
      }
      else
      { throw new RuntimeException(exception);
      }
    }
  }
  
  private void recompile()
  {
    try
    { 
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
