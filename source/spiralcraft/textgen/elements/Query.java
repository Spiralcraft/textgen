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



import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;

import spiralcraft.textgen.FocusElement;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Space;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Queryable;

/**
 * <p>Exposes the results of a query
 * </p>
 * @author mike
 *
 * @param <Tresult>
 */
public class Query<Tresult extends DataComposite>
  extends FocusElement<Tresult>
{
  private spiralcraft.data.query.Query query;
  private Channel<Tresult> resultChannel;
  private Expression<Queryable<?>> queryableX
    =Expression.create("[:"+Space.SPACE_URI+"]");
    
  public void setQuery(spiralcraft.data.query.Query query)
  { this.query=query;
  }
  
  public void setQueryableX(Expression<Queryable<?>> queryableX)
  { this.queryableX=queryableX;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Channel<Tresult> bindSource(Focus<?> focusChain)
    throws BindException
  { 
    Channel<Queryable<?>> queryableChannel=focusChain.bind(queryableX);
    try
    { 
      BoundQuery<?,?> boundQuery
        =queryableChannel.get().query(query,focusChain);
      resultChannel=(Channel<Tresult>) boundQuery.bind();
      return resultChannel;

    }
    catch (DataException x)
    { throw new BindException(this.getErrorContext()+": Error binding query",x);
    }
    
    
  }

  @Override
  protected Focus<?> bindExports(Focus<?> focusChain) throws BindException
  { return focusChain;
  }

  @Override
  protected Tresult compute()
  { return resultChannel.get();
  }

}
