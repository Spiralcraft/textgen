//
// Copyright (c) 2008,2009 Michael Toth
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
package spiralcraft.textgen.elements;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.util.DictionaryBinding;


import spiralcraft.textgen.kit.StandardElement;
import spiralcraft.textgen.kit.TagRenderHandler;

/**
 * <p>Encloses contents in an arbitrary XML tag
 * </p>
 * 
 * @author mike
 */
public class XML
  extends StandardElement
{
  
  private String name;
  private TagRenderHandler renderer
    =new TagRenderHandler()
    {

      @Override
      protected String getName()
      { return name;
      }

      @Override
      protected boolean hasContent()
      {
        // TODO Auto-generated method stub
        return hasChildren();
      }
    };
  
  { addHandler(renderer);
  }


  /**
   * The tag name
   * 
   * @param name
   */
  public void setName(String name)
  { this.name=name;
  }
  
  /**
   * The tag attribute definitions
   * 
   * @param bindings
   */
  public void setAttributeBindings(DictionaryBinding<?>[] bindings)
  { this.renderer.setAttributeBindings(bindings); 
  }
  

  
  @Override
  protected Focus<?> bindExports(Focus<?> focusChain) 
    throws BindException 
  { 

    return focusChain;
  }



  
}
