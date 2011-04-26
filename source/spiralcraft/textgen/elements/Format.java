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

import java.io.IOException;
import java.util.Date;

import spiralcraft.app.Message;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.MessageHandlerChain;
import spiralcraft.textgen.RenderMessage;
import spiralcraft.textgen.kit.AbstractMessageHandler;

/**
 * <p>Formats a date
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public abstract class Format<T extends java.text.Format>
    extends Element
{
  private Expression<?> expression;
  
  private Channel<?> target;
  
  private final ThreadLocal<T> formatLocal
    =new ThreadLocal<T>();
  
  {addHandler(new AbstractMessageHandler()
      { 
        { type=RenderMessage.TYPE;
        }

        @Override
        protected void doHandler(
          EventContext dispatcher,
          Message message,
          MessageHandlerChain next)
        {
          Object val=target.get();
          if (val!=null)
          {
            T format=formatLocal.get();
            if (format==null)
            { 
              format=createFormat();
              formatLocal.set(format);
            }
            updateFormat(format);
            try
            { dispatcher.getOutput().append(format.format(val));
            }
            catch (IOException e)
            { throw new RuntimeException(e);
            }
          }
          next.handleMessage(dispatcher,message);
          
        }
        
      }
    );
  }
  
  public void setX(Expression<?> expression)
  { this.expression=expression;
  }
  
  protected abstract T createFormat();
  
  protected void updateFormat(T format)
  { return;
  }
  
  @Override
  public Focus<?> bind(Focus<?> parentFocus)
    throws ContextualException
  { 
    
    if (expression!=null)
    { target=parentFocus.bind(expression);
    }
    else
    { 
      target
        =new AbstractChannel<Date>
          (BeanReflector.<Date>getInstance(Date.class))
      {

        @Override
        protected Date retrieve()
        { return new Date();
        }

        @Override
        protected boolean store(Date date) throws AccessException
        { return false;
        }
      };
    }
    
    formatLocal.set(createFormat());

    
    return super.bind(parentFocus);
  }
  

}
