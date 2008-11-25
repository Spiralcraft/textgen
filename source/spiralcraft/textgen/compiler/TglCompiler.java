//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.textgen.compiler;

import spiralcraft.data.xml.XmlQueryable;
import spiralcraft.log.ClassLog;
import spiralcraft.text.Trimmer;

import spiralcraft.text.xml.Attribute;
import spiralcraft.text.LookaheadParserContext;
import spiralcraft.text.xml.TagReader;

import spiralcraft.text.io.ResourceCharSequence;
import spiralcraft.text.markup.MarkupCompiler;
import spiralcraft.text.markup.MarkupException;

import spiralcraft.text.ParseException;
import spiralcraft.text.ParsePosition;

import spiralcraft.vfs.Resource;
import spiralcraft.vfs.Resolver;

import java.net.URI;

import java.io.IOException;

/**
 * <P>Compiles a CharSequence containing Text Generation Markup Language
 *   into a tree of Units that can later be bound to an application
 *   context.
 * </P>
 * 
 * <P>Each unit in the tree is either a TglContentUnit, which contains literal
 *   text, or a TglElementUnit, which represents a functionality specified by 
 *   a markup element.
 * </P>
 *   
 * <P>The MarkupCompiler superclass provides the basic mechanism to parse 
 *   an abstract template-style markup language. The TglCompiler further
 *   specifies this mechanism by introducing specific standard delimiters
 *   (&lt;% %&gt;) as well as the concept of open and closed tags.
 * </P>
 * 
 * <P>This TglCompiler is not thread-safe
 * </P>
 */
public class TglCompiler<T extends DocletUnit>
  extends MarkupCompiler<TglUnit>
{
  @SuppressWarnings("unused")
  private static final ClassLog log=ClassLog.getInstance(XmlQueryable.class);
  
  private final Trimmer _trimmer=new Trimmer("\r\n\t ");
  
  public TglCompiler()                
  { 
    super("<%","%>");
  }

  /**
   * Compile a resource
   * 
   * @param sourceURI
   * @return The DocletUnit subtype that represents the compiled resource
   * @throws ParseException
   * @throws IOException
   */
  public T compile(URI sourceURI)
    throws ParseException,IOException
  {
    Resource resource=Resolver.getInstance().resolve(sourceURI);
    if (!resource.supportsRead())
    { throw new IOException("Resource "+resource.getURI()+" is not readable");
    }
    
    CharSequence sequence = new ResourceCharSequence(sourceURI);

    T root=createDocletUnit(null,resource);

    compile(root,sequence,sourceURI);
    return root;
  }
  
  /**
   * Compile a nested resource
   * 
   * @param sourceURI
   * @return The DocletUnit subtype that represents a compiled resource
   * @throws ParseException
   * @throws IOException
   */
  public T subCompile(TglUnit parent,URI sourceURI)
    throws ParseException,IOException
  { 
    Resource resource=Resolver.getInstance().resolve(sourceURI);
    CharSequence sequence = new ResourceCharSequence(sourceURI);

    T root=createDocletUnit(parent,resource);
    // Launch new compiler for subcompile
    clone().compile(root,sequence,sourceURI);
    return root;
    
  }
  
  @Override
  protected TglCompiler<T> clone()
  { return new TglCompiler<T>();
  }
  
  @SuppressWarnings("unchecked") // Default behavior
  protected T createDocletUnit(TglUnit parent,Resource resource)
  { return (T) new DocletUnit(parent,resource);
  }
  
  public ElementFactory createElementFactory
    (URI namespaceUri
    ,String elementName
    ,Attribute[] attributes
    ,PropertyUnit[] properties
    ,ParsePosition parsePosition
    )
    throws MarkupException
  {
    return new AssemblyElementFactory
      (namespaceUri,elementName,attributes,properties,parsePosition);
    
    
  }
  
  @Override
  public void handleContent(CharSequence content)
  { 
    ContentUnit unit=new ContentUnit(getUnit(),content);
    unit.setPosition(position);
    pushUnit(unit);
  }
  
  @Override
  public void handleMarkup(CharSequence code)
    throws MarkupException,ParseException
  { 
    code=_trimmer.trim(code);
    if (code.charAt(0)=='/')
    { 
      // End tag case
      String unitName=code.subSequence(1,code.length()).toString();
      String expectName=getUnit().getName();
      if (unitName.equals(expectName))
      { closeUnit();
      }
      else
      { 
        if (expectName!=null)
        { 
          throw new MarkupException
            ("Mismatched end tag. Found <%/"+unitName+"%>"
            +", expecting <%/"+expectName+"%>"
            ,position
            );
        }
        else
        {
          throw new MarkupException
            ("Unexpected end tag <%/"+unitName+"%>- no tags are open",position);
        }
      }
    }
    else if (code.charAt(0)=='@')
    { pushUnit(parseProcessingUnit(code));
    }
    else if (code.charAt(0)=='.')
    { 
      PropertyUnit propertyUnit
        =new PropertyUnit(getUnit(),code,position);
      pushUnit(propertyUnit);
    }
    else if (code.charAt(0)=='=')
    { 
      ExpressionUnit expressionUnit
        =new ExpressionUnit(getUnit(),code,position);
      pushUnit(expressionUnit);
    }
    else
    {
      ElementUnit tglElementUnit
        =new ElementUnit(getUnit(),this,code,position);
      // log.fine(tglElementUnit.getName()+" open="+tglElementUnit.isOpen());
      pushUnit(tglElementUnit);
    }
  }
  
  protected TglUnit parseProcessingUnit(CharSequence code)
    throws ParseException,MarkupException
  {
    LookaheadParserContext context
      =new LookaheadParserContext(code.toString().substring(1));
    TagReader tagReader=new TagReader();
    tagReader.readTag(context);
    
    String name=tagReader.getTagName();
    Attribute[] attributes=tagReader.getAttributes();

    TglUnit processingUnit=resolveProcessingUnit(name,attributes);
    if (processingUnit==null)
    { throw new MarkupException("Unknown processing unit '"+name+"'",position);
    }
    if (tagReader.isClosed())
    { processingUnit.close();
    }
    else if (!processingUnit.allowsChildren())
    { 
      throw new MarkupException
        (processingUnit.getName()
        +" does not accept content- close tag with '/' "
        ,position
        );
    }
    return processingUnit;
  }
  
  protected TglUnit resolveProcessingUnit(String name,Attribute[] attributes)
    throws MarkupException
  {
    try
    {
      if (name.equals("include"))
      { return new IncludeUnit(getUnit(),this,attributes);
      }
      else if (name.equals("insert"))
      { return new InsertUnit(getUnit(),this,attributes);
      }
      else if (name.equals("define"))
      { return new DefineUnit(getUnit(),this,attributes);
      }
      else if (name.equals("namespace"))
      { return new NamespaceUnit(getUnit(),this,attributes);
      }
      else if (name.equals("comment"))
      { return new CommentUnit(getUnit(),this,attributes);
      }
      else
      { return null;
      }
    }
    catch (ParseException x)
    { throw new MarkupException("Error parsing processing unit",getPosition(),x);
    }
  }

}
