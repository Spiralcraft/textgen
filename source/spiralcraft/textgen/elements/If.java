//
// Copyright (c) 1998,2007 Michael Toth
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
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.ValueState;
import spiralcraft.app.Component;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.InitializeMessage;
import spiralcraft.app.Scaffold;
import spiralcraft.common.ContextualException;


/**
 * <p>Logical element renders contents if the condition (X) is true. If the
 *   condition is false, and an "Else" element is a direct child, the contents
 *   following the Else is rendered.
 * </p>
 * 
 * @author mike
 *
 */
public class If
  extends Element
{
  private Expression<Boolean> expression;
  private Channel<Boolean> target;
  private int elsePos=-1;
  private boolean filterMessages=true;
  private Boolean constValue;
  private boolean hasElse;
  
  /**
   * <p>A boolean filter expression
   * </p>
   * 
   * @param expression
   */
  public void setX(Expression<Boolean> expression)
  { this.expression=expression;
  }
  
  /**
   * <p>Specifies whether multicast messages should be passed through the
   *   filter expression (ie. blocked when the filter expression is "false").
   *   The default value for this property is "true".
   * </p>
   * 
   * <p>Messages targeted at a specific component and the Initialize
   *   message will not be intercepted by this setting.
   * </p>
   * 
   * @param filteMessages
   */
  public void setFilterMessages(boolean filterMessages)
  { this.filterMessages=filterMessages;
  }
  
  @Override
  @SuppressWarnings("unchecked") // Not using generic versions
  protected Focus<?> bindStandard(Focus<?> parentFocus)
    throws ContextualException
  { 
    
    if (expression!=null)
    { 
      if (debug)
      { log.fine("Using "+expression);
      }
      target=parentFocus.<Boolean>bind(expression);
    }
    else
    { 
      if (debug)
      { log.fine("Using parent Focus");
      }
      target=(Channel<Boolean>) parentFocus.getSubject();
    }
    
    if (target.isConstant())
    { 
      constValue=(Boolean.TRUE.equals(target.get()));
      if (debug)
      { log.fine(getDeclarationInfo()+": Target is constant ("+constValue+")");
      }
    }
    
    if (!Boolean.class.isAssignableFrom(target.getContentType())
        && !boolean.class.isAssignableFrom(target.getContentType())
        )
    { throw new BindException
        ("<%If%> requires a boolean expression, not a "+target.getContentType());
    }
    
    super.bindStandard(parentFocus);

    return parentFocus;
  }

  @Override
  protected Component bindChild(int childNum,Scaffold<?> child,Focus<?> focus)
    throws ContextualException
  {
    
    Component childComponent=super.bindChild(childNum,child,focus);
    if (childComponent instanceof Else)
    { 
      if (hasElse)
      { throw new ContextualException("If can only contain one Else");
      }
      elsePos=childNum;
      hasElse=true;
      return childComponent;
    }

    if (constValue==Boolean.TRUE && hasElse)
    { 
      if (debug)
      { log.fine(getDeclarationInfo()+": Discarding unreachable child "+childComponent);
      }
      return null;
    }
    else if (constValue==Boolean.FALSE && !hasElse)
    { 
      if (debug)
      { log.fine(getDeclarationInfo()+": Discarding unreachable child "+childComponent);
      }
      return null;
   }
    
    
    return childComponent;
  }
    
  @Override
  protected void messageStandard
    (Dispatcher context
    ,Message message
    )
  {
    if (message.getType()!=InitializeMessage.TYPE
       && context.isTarget()
       && filterMessages
       )
    {
      
      Boolean val=currentValue(context);
      if (debug)
      { log.fine(getDeclarationInfo()+" :"+expression+":"+message+" (currentValue="+val+")");
      }
      boolean passed=val!=null && val;
    
      int childCount=getChildCount();

      int start;
      int end;
      if (passed)
      { 
        if (debug)
        { log.fine("Condition is true");
        }
        start=0;
        end=elsePos>-1?elsePos:childCount;
      }
      else
      { 
        if (debug)
        { log.fine("Condition is false");
        }
        start=elsePos>-1?elsePos+1:childCount;
        end=childCount;
      }
      

      for (int i=start;i<end;i++)
      { messageChild(i,context,message);
      }
    }
    else
    { 
      if (debug)
      { log.fine(getDeclarationInfo()+" :"+message);
      }
      
      // Initialize event broadcast and targeted events
      //   always get through
      super.messageStandard(context,message);
    }
    
  
  }
  @SuppressWarnings("unchecked")
  protected Boolean currentValue(Dispatcher context)
  {
    Boolean val;
    
    if (!context.isStateful())
    { val=target.get();
    }
    else
    {
      ValueState<Boolean> state=(ValueState<Boolean>) context.getState();
      if (state.isNewFrame() || !state.isValid())
      { 
        val=target.get();
        state.setValue(val);
        if (debug)
        { log.fine(getDeclarationInfo()+" :"+expression+": recomputed currentValue="+val);
        }
      }
      else
      { val=state.getValue();
      }
    }
    return val;
  }
  
  
  @Override
  public ValueState<Boolean> createState()
  { return new ValueState<Boolean>(this);
  }  
}



