package spiralcraft.textgen.compiler;

import java.net.URI;

import java.util.ArrayList;

import spiralcraft.common.namespace.PrefixResolver;

import spiralcraft.text.ParseException;
import spiralcraft.text.ParsePosition;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.text.xml.Attribute;
import spiralcraft.text.LookaheadParserContext;
import spiralcraft.text.xml.TagReader;

/**
 * <P>Abstract base class for standard markup units which accept attributes,
 *   have tag names, and are associated with some form of content.
 * </P>
 * 
 * @author mike
 *
 */
public abstract class MarkupUnit
  extends TglUnit
{
  protected CharSequence markup;
  protected Attribute[] attributes;
  protected boolean open;
  private TglPrefixResolver prefixResolver;

  public MarkupUnit
    (TglUnit parent
    ,CharSequence markup
    ,ParsePosition position
    )
    throws ParseException
  { 
    super(parent);
    setPosition(position.clone());
   
    this.markup=markup;
    readTag();


  }
  
    
  protected void readTag()
    throws ParseException
  {
    LookaheadParserContext context=new LookaheadParserContext(markup.toString());
    TagReader tagReader=new TagReader();
    
    try
    { 
      tagReader.readTag(context);
      if (!context.isEof())
      {
        String remainder
          =markup.toString().substring(1)
            .substring(context.getPosition().getIndex()-1);
        if (remainder.trim().length()>0)
        { 
          throw new MarkupException
            ("Unexpected text in tag ["+remainder.trim()+"]"
            ,getPosition()
            );
        }
      }
    }
    catch (ParseException x)
    { throw new ParseException("Error reading tag",getPosition(),x);
    }


    String name=tagReader.getTagName();

    setName(name);

    Attribute[] tagAttributes=tagReader.getAttributes();
    ArrayList<Attribute> elementAttributes
      =new ArrayList<Attribute>();
    for (Attribute attrib:tagAttributes)
    { 
      if (checkUnitAttribute(attrib))
      { 
      }
      else if (attrib.getName().startsWith("xmlns:"))
      { 
        mapNamespace
          (attrib.getName().substring(6)
          ,URI.create(attrib.getValue())
          );
      }
      else
      { elementAttributes.add(attrib);
      }
    }
    attributes
      =elementAttributes.toArray(new Attribute[elementAttributes.size()]);
    open=!tagReader.isClosed();
    
  }
  
  private void mapNamespace(String prefix,URI namespace)
  { 
    if (prefixResolver==null)
    { 
      if (parent==null)
      { prefixResolver=new TglPrefixResolver();
      }
      else
      { prefixResolver=new TglPrefixResolver(parent.getNamespaceResolver());
      }
      
    }
    prefixResolver.mapPrefix(prefix, namespace);
  }
  
  public Attribute getAttribute(String name)
  {
    for (Attribute attribute: attributes)
    { 
      if (attribute.getName().equals(name))
      { return attribute;
      }
    }
    return null;
  }
  
  public Attribute[] getAttributes()
  { return attributes;
  }
  
  @Override
  public boolean isOpen()
  { return open;
  }
 
  @Override
  public PrefixResolver getNamespaceResolver()
  { 
    if (prefixResolver!=null)
    { return prefixResolver;
    }
    else 
    { return super.getNamespaceResolver();
    }
  }

}
