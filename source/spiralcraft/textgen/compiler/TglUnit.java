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

import java.io.PrintWriter;

import spiralcraft.common.ContextualException;
import spiralcraft.common.namespace.PrefixResolver;
import spiralcraft.common.namespace.QName;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.scaffold.Scaffold;
import spiralcraft.log.ClassLog;
import spiralcraft.textgen.Element;
import spiralcraft.textgen.EventContext;


import spiralcraft.text.ParseException;
import spiralcraft.text.markup.Unit;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.text.xml.Attribute;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

/**
 * A Unit of text generation which represents a
 *   node in the tree structure of a TGL block.
 */
public abstract class TglUnit
  extends Unit<TglUnit>
  implements Scaffold<TglUnit,Element,ContextualException>
{
  protected final ClassLog log
    =ClassLog.getInstance(getClass());
  protected boolean allowsChildren=true;
  protected boolean trim;
  protected boolean debug;
  private boolean exported;

  protected HashMap<String,TglUnit> defines;

  private TglPrefixResolver prefixResolver;
  
  public static final URI DEFAULT_ELEMENT_PACKAGE
    =URI.create("class:/spiralcraft/textgen/elements/");
  
  public TglUnit(TglUnit parent)
  { super(parent);
  }
  
  @Override
  public TglUnit getParent()
  { return parent;
  }
  
  public boolean isExported()
  { return exported;
  }

  // only called once to reset exported after exporting
  void setExported(boolean exported)
  { this.exported=exported; 
  }
  
  public void define(String name,TglUnit unit)
  {
    if (defines==null)
    { defines=new HashMap<String,TglUnit>();
    }
    
    defines.put(name, unit);
  }
  
  /**
   * Export defines to the parent
   */
  public void exportDefines()
  { 
    if (defines==null)
    { return;
    }
    for (String name: defines.keySet())
    { 
      TglUnit define=defines.get(name);
      if (define.isExported())
      { 
        parent.define(name,define);
        define.setExported(false);
      }
    }
  }
  
  /**
   * Export defines to a unit that is inserting this unit. Only defines marked for export will be
   *   exported, and remain available for export to units that further insert the target.
   */
  public void exportDefines(TglUnit target)
  { 
    if (defines==null)
    { return;
    }
    for (String name: defines.keySet())
    { 
      TglUnit define=defines.get(name);
      if (define.isExported())
      { target.define(name,define);
      }
    }
  } 
  
  protected void initPrefixResolver()
  {
    if (parent==null)
    { prefixResolver=new TglPrefixResolver();
    }
    else
    { prefixResolver=new TglPrefixResolver(parent.getNamespaceResolver());
    }
  }
    
  private void mapNamespace(String prefix,URI namespace)
  { 
    if (prefixResolver==null)
    { initPrefixResolver();
    }
    prefixResolver.mapPrefix(prefix, namespace);
  }
  
  /**
   * A tag name in the form <code>&lt;%namespace:name ... %&gt;</code> 
   * 
   * @throws ParseException
   */
  protected QName resolvePrefixedName(String name,URI defaultPackage)
    throws ParseException
  { 
        
    int nspos=name.indexOf(':');
    if (nspos>-1)
    {  
      PrefixResolver resolver
        =getNamespaceResolver();
      
      URI elementPackage
        = resolver!=null
        ? resolver.resolvePrefix(name.substring(0,nspos))
        : null
        ;
           
      if (elementPackage==null)
      { 
        throw new ParseException
          ("Namespace prefix '"+name.substring(0,nspos)+"' not found"
          ,getPosition()
          );
      }
      
      return new QName(elementPackage,name.substring(nspos+1));
    }
    else
    { 
      return new QName(defaultPackage,name);
    }
  }  
  
  /**
   * Finds a unit that is an ancestor in the containership hierarchy within
   *   the scope of the current document.
   * 
   * @param unitClass
   * @return
   */
  @SuppressWarnings("unchecked")
  public <X extends TglUnit> X findUnitInDocument(Class<X> unitClass)
  { 
    if (getClass().isAssignableFrom(unitClass))
    { return (X) this;
    }
    else if (parent!=null)
    { return parent.findUnitInDocument(unitClass);
    }
    else
    { return null;
    }
  }
  
  public TglUnit findDefinition(String name)
  {
    // Parent takes precedence
    TglUnit ret=parent!=null?parent.findDefinition(name):null;
    if (ret==null && defines!=null)
    { ret=defines.get(name);
    }
    return ret;
      
//    // Local takes precedence 
//    // (deprecated- parent precedence facilitates inheritance)
//    DefineUnit ret=defines!=null?defines.get(name):null;
//    if (ret==null && parent!=null)
//    { return parent.findDefinition(name);
//    }
//    return ret;
  }
  
  public boolean allowsChildren()
  { return allowsChildren;
  }
    
  /**
   * <p>Create a tree of Elements bound into an application context
   *   (the Assembly) which implements the functional behavior 
   *   specified by the TGL document.
   * </p>
   */
  @Override
  public Element bind(Focus<?> focus,Element parentElement)
    throws ContextualException
  { return bind(focus,parentElement,createElement());
  }

  protected Element createElement()
  { return new DefaultElement();
  }
  
  /**
   * Extend this Unit by applying the specified set of attributes and using the supplied children as content
   *   instead of this Unit's own children.
   * 
   * @param attribs
   * @param focus
   * @param parentElement
   * @param children
   * @return
   * @throws MarkupException
   */
  public Element bindExtension(Attribute[] attribs,Focus<?> focus,Element parentElement,List<TglUnit> children)
    throws ContextualException
  { 
    if (attribs!=null && attribs.length>0)
    { throw new MarkupException("Unrecognized attribute "+attribs[0].getName(),getPosition());
    }
    if (children!=null && children.size()>0)
    { log.warning("Ignoring contents of element defined at "+getPosition());
    }
    return bind(focus,parentElement);
  }

  public void dumpTree(PrintWriter writer,String linePrefix)
  { 
    writer.println(linePrefix+toString());
    for (TglUnit unit: children)
    { unit.dumpTree(writer,linePrefix+"  ");
    }
  }  
  
  protected Element defaultBind(Focus<?> focus,Element parentElement)
    throws ContextualException
  { return bind(focus,parentElement,new DefaultElement());
  }
  
  
  protected Element bind
    (Focus<?> focus
    ,Element parentElement
    ,Element unboundElement
    )
    throws ContextualException
  {
    unboundElement.setParent(parentElement);
    unboundElement.setScaffold(this);
    try
    { unboundElement.bind(focus);
    }
    catch (RuntimeException x)
    { throw new MarkupException(x.toString(),getPosition(),x);
    }
    catch (BindException x)
    { throw new MarkupException(x.toString(),getPosition(),x);
    }
    return unboundElement;
  }
  
  protected boolean checkUnitAttribute(Attribute attrib)
    throws ParseException
  {
    if (attrib.getName().startsWith("textgen:"))
    { 
      
      addUnitAttribute
        (attrib.getName().substring(8),attrib.getValue());
      return true;

    }
    else if (attrib.getName().startsWith("tgns:"))
    { 
      mapNamespace
        (attrib.getName().substring(5)
        ,URI.create(attrib.getValue())
        );
      return true;
    }
    return false;
  }
  
  protected void addUnitAttribute(String name,String value)
    throws ParseException
  { 
    name=name.intern();
    if (name.equals("trim"))
    { trim=Boolean.parseBoolean(value);
    }
    else if (name.equals("debug"))
    { debug=Boolean.parseBoolean(value);
    }
    else
    { 
      throw new ParseException
        ("Unrecognized textgen attribute '"+name
        +"' - not one of {trim,debug}"
        ,getPosition()
        );
    }
    
  }
  
  /**
   * <p>Whether whitespace should be trimmed from any content blocks directly
   *   contained in this Unit.
   * </p>
   * 
   * <p>The default value is false, but this directive will be handled
   *   according to the specific subtype of TglUnit in use.
   * </p>
   * 
   * @return Whether to trim whitespace or not
   */
  public boolean getTrim()
  { return trim;
  }
  
  /**
   * 
   * @return The NamespaceResolver which provides namespace mappings
   *   currently in effect.
   */
  public PrefixResolver getNamespaceResolver()
  { 
    if (prefixResolver!=null)
    { return prefixResolver;
    }
    else if (parent!=null)
    { return parent.getNamespaceResolver();
    }
    return null;
  }  
  
  protected DocletUnit includeResource(String qname,TglCompiler<?> compiler)
    throws MarkupException
  {

    String resourceRef;
    if (qname.startsWith(":"))
    { 
      // Translate namsepace prefix
      String prefix=qname.substring(1,qname.indexOf(":",1));
      String suffix=qname.substring(prefix.length()+2);
      PrefixResolver resolver=getNamespaceResolver();
      if (resolver!=null)
      {
        URI uri=resolver.resolvePrefix(prefix);
        if (uri!=null)
        {
          if (!uri.getPath().endsWith("/"))
          { uri=URI.create(uri.toString()+"/");
          }
          uri=uri.resolve(suffix);
          resourceRef=uri.toString();
        }
        else
        { 
          throw new MarkupException
          ("Namespace prefix '"+prefix+"' not defined"
            ,compiler.getPosition()
          );
        }

      }
      else
      { 
        throw new MarkupException
        ("No namespace prefixes defined: resolving '"+prefix+"'- parent is "+parent
          ,compiler.getPosition()
        );
      }
    }
    else
    { resourceRef=qname;
    }


    URI resourceURI=null;
    try
    { resourceURI=new URI(resourceRef);
    }
    catch (URISyntaxException x)
    { 
      throw new MarkupException
      ("Error creating URI '"+resourceRef+"':"+x
        ,compiler.getPosition()
      );
    }


    if (!resourceURI.isAbsolute())
    {
      DocletUnit parentDoc=findUnit(DocletUnit.class);
      URI baseURI=parentDoc.getSourceURI();
      resourceURI=baseURI.resolve(resourceURI);

    }

    try
    { 
      // This will add the Unit defined by the specified resource
      //   as the first child of this unit, and return the unit
      return compiler.subCompile(this,resourceURI);
    }
    catch (ParseException x)
    { 

      throw new MarkupException
      ("Error including URI '"+resourceRef+"':"+x
        ,compiler.getPosition()
        ,x
      );
    }
    catch (IOException x)
    {
      throw new MarkupException
      ("Error including URI '"+resourceRef+"':"+x
        ,compiler.getPosition()
        ,x
      );
    }
  }
  
  
}

class DefaultElement
  extends Element
{
  
  @Override
  public void render(EventContext context)
    throws IOException
  { renderChildren(context);
  }
}
