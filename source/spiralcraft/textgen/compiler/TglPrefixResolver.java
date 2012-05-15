//
// Copyright (c) 2009,2009 Michael Toth
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

import java.net.URI;
import java.util.Map;

import spiralcraft.common.namespace.PrefixResolver;
import spiralcraft.common.namespace.StandardPrefixResolver;

/**
 * <p>Fixes the default namespace
 * </p>
 * 
 * @author mike
 *
 */
public class TglPrefixResolver
    extends StandardPrefixResolver
{
  public TglPrefixResolver()
  {
  }
  
  public TglPrefixResolver(PrefixResolver parent)
  { super(parent);
  }
  
  @Override
  public URI resolvePrefix(String name)
  { 
    if (!name.equals(""))
    { return super.resolvePrefix(name);
    }
    else
    { 
      URI ret=super.resolvePrefix("");
      return ret!=null?ret:TglUnit.DEFAULT_ELEMENT_PACKAGE;
    }
  }
  
  @Override
  public Map<String,URI> computeMappings()
  { 
    Map<String,URI> computedMappings=super.computeMappings();
    computedMappings.put("",TglUnit.DEFAULT_ELEMENT_PACKAGE);
    return computedMappings;
  }    
}
