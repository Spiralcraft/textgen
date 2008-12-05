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

import spiralcraft.textgen.ElementState;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.InitializeMessage;

import spiralcraft.textgen.Message;

import spiralcraft.textgen.compiler.TglUnit;

import spiralcraft.text.markup.MarkupException;

import java.io.IOException;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>Logical element renders contents if the condition (X) is true. If the
 *   condition is false, and an "Else" element is a direct child, the contents
 *   following the Else is rendered.
 * </p>
 * 
 * XXX: We need to evaluate the filter on prepare, and cache it for render and
 *   later events if this component is stateful. This may require some sort
 *   of "invalidate" notification to signal a re-evaluation requirement
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
  public void bind(List<TglUnit> childUnits)
    throws BindException,MarkupException
  { 
    Focus<?> parentFocus=getParent().getFocus();
    
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
    
    
    
    if (!Boolean.class.isAssignableFrom(target.getContentType())
        && !boolean.class.isAssignableFrom(target.getContentType())
        )
    { throw new BindException
        ("<%If%> requires a boolean expression, not a "+target.getContentType());
    }
    
    bindChildren(childUnits);
    int childCount=getChildCount();
    for (int i=0;i<childCount;i++)
    { 
      if (getChild(i) instanceof Else)
      { 
        elsePos=i;
        break;
      }
    }
  }
  
  @Override
  public void message
    (EventContext context
    ,Message message
    ,LinkedList<Integer> path
    )
  {
    if (message.getType()!=InitializeMessage.TYPE
       && (path==null || path.isEmpty())
       && filterMessages
       )
    {
      
      Boolean val=target.get();
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
      { messageChild(i,context,message,path);
      }
    }
    else
    { 
      // Initialize event broadcast and targeted events
      //   always get through
      super.message(context,message,path);
    }
    
  
  }
  
  /**
   * <P>Renders the tag. A positive result is displayed only if the bound
   *   expression returns true. A null value is interpreted as false.
   * </P>
   */
  @Override
  public void render(EventContext context)
    throws IOException
  { 
    
    
    
    Boolean val=target.get();
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
    { renderChild(context,i);
    }
      
  }
  
  @Override
  public IfState createState()
  { return new IfState(getChildCount());
  }  
}

class IfState
  extends ElementState
{
  private Boolean result;
  
  public IfState(int childCount)
  { super(childCount);
  }
  
  public Boolean getResult()
  { return result;
  }
  
  public void setResult(Boolean result)
  { this.result=result;
  }
}


