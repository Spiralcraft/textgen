package spiralcraft.textgen.compiler;

import java.util.ArrayList;

import spiralcraft.text.ParseException;
import spiralcraft.text.ParsePosition;
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
    { tagReader.readTag(context);
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
      if (attrib.getName().startsWith("textgen:"))
      { 
        addUnitAttribute
          (attrib.getName().substring(8),attrib.getValue());
      }
      else
      { elementAttributes.add(attrib);
      }
    }
    attributes
      =elementAttributes.toArray(new Attribute[elementAttributes.size()]);
    open=!tagReader.isClosed();
    
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
  
  @Override
  public boolean isOpen()
  { return open;
  }

}
