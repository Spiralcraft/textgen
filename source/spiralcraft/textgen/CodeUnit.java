package spiralcraft.textgen;

import spiralcraft.util.StringUtil;

import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.Assembly;
import spiralcraft.builder.PropertySpecifier;
import spiralcraft.builder.BuildException;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;

import spiralcraft.xml.Attribute;
import spiralcraft.xml.ParserContext;
import spiralcraft.xml.TagReader;

import java.net.URI;

import java.io.Writer;
import java.io.IOException;

/**
 * A Unit which contains literal text
 */
public class CodeUnit
  extends Unit
{
  private static final URI _DEFAULT_TAG_PACKAGE
    =URI.create("java:/spiralcraft/textgen/tags/");
  
  private final CharSequence _code;
  private boolean _open=true;
  private AssemblyClass _assemblyClass;
  private URI _tagPackage;
  private String _tagName;
  private Attribute[] _attributes;
  
  private Expression _expression;
  
  
  public CodeUnit(CharSequence code)
    throws ParseException
  { 
    _open=!(code.charAt(code.length()-1)=='/');
    if (_open)
    { _code=code;
    }
    else
    { _code=code.subSequence(0,code.length()-1);
    }
    
    if (_code.charAt(0)=='=')
    { readExpressionTag();
    }
    else
    { readStandardTag();
    }
    if (!_open)
    { close();
    }
  }
  
  private void readExpressionTag()
    throws ParseException
  { 
    CharSequence expressionText;
    if (_code.charAt(1)=='=')
    { expressionText=_code.subSequence(2,_code.length());
    }
    else
    { expressionText=_code.subSequence(1,_code.length());
    }
    
    try
    { _expression=Expression.parse(expressionText.toString());
    }
    catch (spiralcraft.lang.ParseException x)
    { throw new ParseException(x);
    }
  }
  
  private void readStandardTag()
    throws ParseException
  { 
    
    try
    {
      ParserContext context=new ParserContext(_code.toString());
      TagReader tagReader=new TagReader();
      tagReader.readTag(context);
          
      
      String name=tagReader.getTagName();
      setName(name);
      int nspos=name.indexOf(':');
      if (nspos>-1)
      { 
        _tagPackage=resolveNamespace(name.substring(0,nspos));
        _tagName=name.substring(nspos+1);
      }
      else
      { 
        _tagPackage=_DEFAULT_TAG_PACKAGE;
        _tagName=name;
      }
      _attributes=tagReader.getAttributes();
    }
    catch (spiralcraft.xml.ParseException x)
    { throw new ParseException(x);
    }

  }
  
  private URI resolveNamespace(String namespaceId)
    throws ParseException
  { throw new ParseException("Unknown namespace "+namespaceId);
  }
  
  public boolean isOpen()
  { return _open;
  }

  public void close()
    throws ParseException
  {
    _open=false;
    if (_expression==null)
    {
      try
      {
        _assemblyClass=new AssemblyClass
          (null
          ,_tagPackage
          ,Character.toUpperCase(_tagName.charAt(0))+_tagName.substring(1)
          ,null
          ,null
          );

        if (_attributes!=null)
        { 
          for (int i=0;i<_attributes.length;i++)
          { 
            _assemblyClass.addPropertySpecifier
              (new PropertySpecifier
                (_assemblyClass
                ,_attributes[i].getName()
                ,_attributes[i].getValue()
                )
              );
          }
        }
        _assemblyClass.resolve();
      }
      catch (BuildException x)
      { throw new ParseException(x);
      }
    }
  }

  public Tag bind(Assembly parent,Tag parentTag)
    throws BuildException,BindException
  { 
    if (_expression!=null)
    { 
      Tag tag=new ExpressionTag();
      tag.bind(parentTag);
      return tag;
    }
    else
    {
      Assembly assembly=_assemblyClass.newInstance(parent);
      Tag tag=(Tag) assembly.getSubject().get();
      
      tag.bind(parentTag);
      bindChildren(assembly,tag);
      return tag;
    }
  }

  public String toString()
  { return super.toString()+"[name="+getName()+"]";
  }
  
  class ExpressionTag
    extends Tag
  { 
    
    private Channel _source;
    
    public void bind(Tag parent)
      throws BindException
    { 
      super.bind(parent);
      _source=getFocus().bind(_expression);
    }
    
    public void write(Writer out)
      throws IOException
    { 
      Object value=_source.get();
      if (value!=null)
      { out.write(value.toString());
      }
    }
  }
}
