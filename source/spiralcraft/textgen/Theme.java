//
// Copyright (c) 2012 Michael Toth
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
import java.util.HashMap;

import spiralcraft.common.ContextualException;
import spiralcraft.data.Tuple;
import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.data.sax.DataReader;
import spiralcraft.log.ClassLog;
import spiralcraft.util.URIUtil;
import spiralcraft.vfs.Container;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;

/**
 * <p>A Theme is a collection of textgen components (.tgl files) and other
 *   resources that define the visual aspects of an app.
 * </p>
 * 
 * <p>A Theme may extend a base Theme and provide customized implementations
 *   of some or all of the base Theme's components.
 * <p>
 *   
 * @author mike
 */
public class Theme
{
  private static final ClassLog log
    =ClassLog.getInstance(Theme.class);
  
  
  private static final HashMap<URI,Theme> map
    =new HashMap<URI,Theme>();
  
  public static final synchronized Theme fromContainer(Container container)
    throws ContextualException
  {
    
    if (container==null)
    { return null;
    }
    Theme ret=map.get(container.getURI());
    if (ret==null && !map.containsKey(container.getURI()))
    {
      try
      {
        
        Resource resource=container.getChild("Theme.xml");
        if (resource.exists())
        {
          Tuple themeTuple
            =(Tuple) new DataReader()
              .readFromResource
                (resource
                ,ReflectionType.canonicalType(Theme.class)
                );
          ret=(Theme) themeTuple.getType().fromData(themeTuple,null);
          ret.uri=container.getURI();
        }
        else
        { 
          Resource parent=container.getParent();
          if (parent!=null)
          { ret=fromContainer(parent.asContainer());
          }
          else
          { ret=null;
          }
        }
      }
      catch (Exception x)
      { throw new ContextualException("Error reading theme",x);
      }
    }
    map.put(container.getURI(),ret);
    return ret;
  }
  
  private URI uri;
  private URI base;
  
  public URI getBase()
  { return base;
  }
  
  public void setBase(URI base)
  { this.base=URIUtil.ensureTrailingSlash(base);
  }
  
  public Resource baseResource(Resource overlayResource)
    throws IOException
  {
    if (base==null)
    { return null;
    }
    
    URI baseURI=base.resolve(uri.relativize(overlayResource.getURI()));
    log.fine("Theme in "+uri+" based "+overlayResource.getURI()+" to "+baseURI);
    if (baseURI!=null)
    { return Resolver.getInstance().resolve(baseURI);
    }
    else
    { return null;
    }
  }
  
  
}
