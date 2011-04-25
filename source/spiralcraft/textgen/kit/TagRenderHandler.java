package spiralcraft.textgen.kit;

import java.io.IOException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.util.DictionaryBinding;
import spiralcraft.text.xml.AttributeEncoder;
import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.MessageHandler;
import spiralcraft.textgen.MessageHandlerChain;
import spiralcraft.textgen.RenderMessage;

import spiralcraft.app.Message;

public abstract class TagRenderHandler
  implements MessageHandler
{ 
  protected abstract String getName();
  
  protected abstract boolean hasContent();

  private DictionaryBinding<?>[] attributeBindings;

  private AttributeEncoder attributeEncoder = new AttributeEncoder();
  
  public void setAttributeBindings(DictionaryBinding<?>[] attributeBindings)
  { this.attributeBindings=attributeBindings;
  }
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws BindException
  {
    if (attributeBindings!=null)
    { 
      for (DictionaryBinding<?> binding : attributeBindings)
      { binding.bind(focusChain);
      }
    }
    return focusChain;
  }
  
  @Override
  public void handleMessage(
    EventContext context,
    Message message,
    MessageHandlerChain next)
  {

    if (message.getType()==RenderMessage.TYPE)
    {
      try
      {
        Appendable writer=context.getOutput();
        
        String tagName=getName();
        
        writer.append("<");
        writer.append(tagName);
    
        renderAttributes(writer);
    
        boolean hasContent=hasContent();
        if (hasContent)
        { 
          writer.append(">");
          next.handleMessage(context,message);
        }
        else
        { writer.append("/>");
        }
        
        if (hasContent)
        {
          writer.append("</");
          writer.append(tagName);
          writer.append(">");
        } 
      }
      catch (IOException x)
      { throw new RuntimeException("Error rendering tag ",x);
      }
    }
    else
    { next.handleMessage(context,message);
    }
    
  }  
  
  private void renderAttributes(Appendable writer)
    throws IOException
  { 
    if (attributeBindings!=null)
    { 
      for (DictionaryBinding<?> binding:attributeBindings)
      {
        String value=binding.get();
        if (value!=null)
        { 
          writer.append(" ");
          writer.append(binding.getName());
          writer.append(" = \"");
          attributeEncoder.encode(binding.get(),writer);
          writer.append("\"");
          
        }
      }
      
    }
      
  }
  
}
