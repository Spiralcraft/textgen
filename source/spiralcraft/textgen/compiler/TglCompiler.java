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

import spiralcraft.common.namespace.StandardPrefixResolver;
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
import spiralcraft.util.ContextDictionary;
import spiralcraft.util.string.StringPool;

import spiralcraft.vfs.Resource;
import spiralcraft.vfs.Package;

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
  private static final ClassLog log=ClassLog.getInstance(TglCompiler.class);
  
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
    Resource resource=Package.findResource(sourceURI);
    if (resource==null)
    { throw new IOException("Resource not found: "+sourceURI);
    }
    
    if (!resource.supportsRead())
    { throw new IOException("Resource "+resource.getURI()+" is not readable");
    }
    
    CharSequence sequence = new ResourceCharSequence(resource);

    this.position=new ParsePosition();
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
    Resource resource=Package.findResource(sourceURI);
    if (resource==null)
    { throw new IOException("Resource not found: "+sourceURI);
    }
    
    CharSequence sequence = new ResourceCharSequence(resource);

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
  { return (T) new DocletUnit(parent,resource,this);
  }
  
  public ElementFactory createElementFactory
    (URI namespaceUri
    ,String elementName
    ,Attribute[] attributes
    ,PropertyUnit[] properties
    ,ParsePosition parsePosition
    ,StandardPrefixResolver prefixResolver
    ,ElementUnit elementUnit
    )
    throws MarkupException
  {
    return new ElementFactory
      (namespaceUri,elementName,attributes,properties,parsePosition,prefixResolver,elementUnit);
    
    
  }
  
  @Override
  public void handleContent(CharSequence content)
  { 
    ContentUnit unit=new ContentUnit(getUnit(),content,this);
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
    else if (code.charAt(0)=='$')
    { pushUnit(parseDefineUnit(code));
    }   
    else if (code.charAt(0)=='!')
    { 
      CommentUnit commentUnit
        =new CommentUnit(getUnit(),this);
      pushUnit(commentUnit);
    }    
    else if (code.charAt(0)=='.')
    { 
      PropertyUnit propertyUnit
        =new PropertyUnit(getUnit(),code,this);
      pushUnit(propertyUnit);
    }
    else if (code.charAt(0)=='=')
    { 
      ExpressionUnit expressionUnit
        =new ExpressionUnit(getUnit(),code,this);
      pushUnit(expressionUnit);
    }
    else if (pushInsert(code))
    {
    }
    else
    {
      ElementUnit tglElementUnit
        =new ElementUnit(getUnit(),this,code);
      // log.fine(tglElementUnit.getName()+" open="+tglElementUnit.isOpen());
      pushUnit(tglElementUnit);
    }
  }
  
  protected TagReader readTag(LookaheadParserContext context)
    throws ParseException
  {
    TagReader tagReader=new TagReader();
    try
    { 
      tagReader.readTag(context);     
      return tagReader;
    }
    catch (ParseException x)
    { throw new ParseException(position,x);
    }
  }
  
  protected boolean pushInsert(CharSequence code)
    throws MarkupException,ParseException
  { 
    LookaheadParserContext context
      =new LookaheadParserContext(code);
    TagReader tagReader=readTag(context);
    if (!context.isEof())
    {
      String remainder
        =code.toString().substring(context.getPosition().getIndex()-1);
      if (remainder.trim().length()>0)
      { 
        if (tagReader.getTagName().equals(""))
        { 
          throw new MarkupException
            ("Unexpected text after tag close ["+remainder.trim()+"]. Perhaps "
            +"you meant <%/@"+remainder+"%> ?"
            ,getPosition()
            );
        }
        else
        {
          throw new MarkupException
            ("Unexpected text after tag close ["+remainder.trim()+"]"
            ,getPosition()
            );
        }
      }
    }
  
    String name=tagReader.getTagName();
    
    if (isInsertable(name))
    {
      Attribute[] attributes=contextualizeAttributes(tagReader.getAttributes());
      InsertUnit processingUnit
        =new InsertUnit(getUnit(),this,attributes,name);
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
      pushUnit(processingUnit);
      return true;
      
    }
    else
    { return false;
    }
  
    
    
  }
  
  private Attribute[] contextualizeAttributes(Attribute[] raw)
    throws MarkupException
  {
    if (raw==null)
    { return null;
    }
    Attribute[] ret=new Attribute[raw.length];
    int i=0;
    for (Attribute attribute: raw)
    { 
      try
      {
        ret[i++]
          =new Attribute
            (attribute.getName()
            ,StringPool.INSTANCE.get(ContextDictionary.substitute(attribute.getValue()))
            );
      }
      catch (ParseException x)
      { throw new MarkupException
          ("Invalid context substitution in "+attribute.getName()+"="+attribute.getValue()
          ,position
          ,x
          );
      }
    }
    return ret;
    
  }

  private boolean isInsertable(String name)
  {
    int colonPos=name.indexOf(":");
    String localName=colonPos==-1?name:name.substring(colonPos+1);
    
    // XXX need to find a better way to distinguish inserts from elements
    if (Character.isLowerCase(localName.charAt(0)))
    { return true;
    }
    else return false;
  }
  
  protected TglUnit parseDefineUnit(CharSequence code)
    throws ParseException,MarkupException
  {

    LookaheadParserContext context
      =new LookaheadParserContext(code.toString().substring(1));
    TagReader tagReader=readTag(context);
    if (!context.isEof())
    {
      String remainder
      =code.toString().substring(1)
      .substring(context.getPosition().getIndex()-1);
      if (remainder.trim().length()>0)
      { 
        if (tagReader.getTagName().equals(""))
        { 
          throw new MarkupException
          ("Unexpected text after tag close ["+remainder.trim()+"]. Perhaps "
            +"you meant <%/$"+remainder+"%> ?"
            ,getPosition()
          );
        }
        else
        {
          throw new MarkupException
          ("Unexpected text after tag close ["+remainder.trim()+"]"
            ,getPosition()
          );
        }
      }
    }

    String name=tagReader.getTagName();
    Attribute[] attributes=tagReader.getAttributes();

    TglUnit processingUnit=new DefineUnit(getUnit(),this,attributes,"$"+name);

    if (tagReader.isClosed())
    { processingUnit.close();
    }
    return processingUnit;
  }

  protected TglUnit parseProcessingUnit(CharSequence code)
    throws ParseException,MarkupException
  {
    
    LookaheadParserContext context
      =new LookaheadParserContext(code.toString().substring(1));
    TagReader tagReader=readTag(context);
    if (!context.isEof())
    {
      String remainder
        =code.toString().substring(1)
          .substring(context.getPosition().getIndex()-1);
      if (remainder.trim().length()>0)
      { 
        if (tagReader.getTagName().equals(""))
        { 
          throw new MarkupException
            ("Unexpected text after tag close ["+remainder.trim()+"]. Perhaps "
            +"you meant <%/@"+remainder+"%> ?"
            ,getPosition()
            );
        }
        else
        {
          throw new MarkupException
            ("Unexpected text after tag close ["+remainder.trim()+"]"
            ,getPosition()
            );
        }
      }
    }
    
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
      if (name.equals("doclet"))
      { return new RootUnit(getUnit(),this,attributes);
      }
      else if (name.equals("include"))
      { return new IncludeUnit(getUnit(),this,attributes);
      }
      else if (name.equals("insert"))
      { return new InsertUnit(getUnit(),this,attributes,"@insert");
      }
      else if (name.equals("define"))
      { return new DefineUnit(getUnit(),this,attributes,"@define");
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
