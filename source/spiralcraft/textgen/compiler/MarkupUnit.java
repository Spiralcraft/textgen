package spiralcraft.textgen.compiler;

import java.util.ArrayList;


import spiralcraft.text.ParseException;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.text.xml.Attribute;
import spiralcraft.text.LookaheadParserContext;
import spiralcraft.text.xml.TagReader;
import spiralcraft.util.string.StringPool;

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
  protected String markup;
  protected Attribute[] attributes;
  protected boolean open;

  public MarkupUnit
    (TglUnit parent
    ,CharSequence markup
    ,TglCompiler<?> compiler
    )
    throws ParseException
  { 
    super(parent,compiler);
   
    this.markup=StringPool.INSTANCE.get(markup.toString());
    readTag();


  }
  
    
  protected void readTag()
    throws ParseException
  {
    LookaheadParserContext context=new LookaheadParserContext(markup);
    TagReader tagReader=new TagReader();
    
    try
    { 
      tagReader.readTag(context);
      if (!context.isEof())
      {
        String remainder
          =markup.substring(1)
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


    String name=StringPool.INSTANCE.get(tagReader.getTagName());

    setName(name);

    Attribute[] tagAttributes=tagReader.getAttributes();
    ArrayList<Attribute> elementAttributes
      =new ArrayList<Attribute>();
    for (Attribute attrib:tagAttributes)
    { 
      if (checkUnitAttribute(attrib))
      { 
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
  
  public Attribute[] getAttributes()
  { return attributes;
  }
  
  @Override
  public boolean isOpen()
  { return open;
  }
 
}
