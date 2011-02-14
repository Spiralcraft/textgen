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
package spiralcraft.textgen.elements;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.compiler.TglUnit;

/**
 * <p>Computes a value at bind time and publishes the result
 * </p>
 * 
 * @author mike
 *
 * @param <Tresult>
 */
public class Constant<Tresult>
  extends Element
{

  private Expression<Tresult> x;
  private URI alias;
  
  public void setX(Expression<Tresult> x)
  { this.x=x;
  }
  
  /**
   * An unique URI under which this value will be published into the
   *   focus chain. 
   * 
   * @param alias
   */
  public void setAlias(URI alias)
  { this.alias=alias;
  }
    
  @Override
  public void render(EventContext context)
    throws IOException
  { renderChildren(context);
  }
  
  @Override
  public final void bind(Focus<?> focus,List<TglUnit> childUnits)
    throws BindException,MarkupException
  { 
    Channel<Tresult> temp=focus.bind(x);
    focus
      =focus.chain
        (new SimpleChannel<Tresult>
          (temp.getReflector()
          ,temp.get()
          ,true
          )
        );
    if (alias!=null)
    { focus.addAlias(alias);
    }
    
    super.bind(focus,childUnits);
  }

  
}
