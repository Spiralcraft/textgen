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
import spiralcraft.common.namespace.ContextualName;
import spiralcraft.common.namespace.NamespaceContext;
import spiralcraft.common.namespace.PrefixResolver;
import spiralcraft.common.namespace.QName;
import spiralcraft.common.namespace.StandardPrefixResolver;
import spiralcraft.common.namespace.UnresolvedPrefixException;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Setter;
import spiralcraft.lang.kit.ConstantChannel;
import spiralcraft.lang.parser.Struct;
import spiralcraft.lang.util.DictionaryBinding;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.textgen.Element;
import spiralcraft.app.Component;
import spiralcraft.app.Parent;
import spiralcraft.app.Scaffold;


import spiralcraft.text.ParseException;
import spiralcraft.text.markup.Unit;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.text.xml.Attribute;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.URIUtil;
import spiralcraft.util.refpool.URIPool;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * A Unit of text generation which represents a
 *   node in the tree structure of a TGL block.
 */
public abstract class TglUnit
  extends Unit<TglUnit>
  implements Scaffold<TglUnit>
{

  public static final URI DEFAULT_ELEMENT_PACKAGE
    =URIPool.create("class:/spiralcraft/textgen/elements/");

  protected PropertyUnit[] properties;  
  protected Expression<?> contextX;
    
  protected final ClassLog log
    =ClassLog.getInstance(getClass());
  protected Level logLevel
    =ClassLog.getInitialDebugLevel(getClass(),Level.INFO);
  
  protected boolean allowsChildren=true;
  protected Boolean trim;
  protected boolean debug=false;
  private boolean exported;
  protected final TglCompiler<?> compiler;

  protected HashMap<String,TglUnit> defines;

  private TglPrefixResolver prefixResolver;
  protected boolean namespaceRoot=false;
  protected URI referencedURI;
  private LinkedList<URI> aliases;

  
  public TglUnit(TglUnit parent,TglCompiler<?> compiler)
  { 
    super(parent);
    this.compiler=compiler;
    setPosition(compiler.getPosition().clone());
    if (parent!=null)
    { trim=parent.getTrim();
    }
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
    if (parent==null || namespaceRoot)
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
   * The actual URI that was used to instantiate this unit
   * 
   * @param uri
   */
  void setReferencedURI(URI uri)
  { this.referencedURI=uri;
  }
  
  /**
   * Aliases that the contextX parameter block can referenced by
   * 
   * @param alias
   */
  public void addAlias(URI alias)
  { 
    if (aliases==null)
    { aliases=new LinkedList<URI>();
    }
    aliases.add(alias);  
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
          ("Namespace prefix '"+name.substring(0,nspos)+"' not found "
            +(resolver!=null?(""+resolver.computeMappings()):"")
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
    if (unitClass.isAssignableFrom(getClass()))
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

  }
  
  public boolean allowsChildren()
  { return allowsChildren;
  }
    
  
  public CharSequence getContent()
  { 
    if (children!=null)
    { 
      StringBuffer buf=new StringBuffer();
      for (TglUnit child:children)
      { 
        if (child instanceof ContentUnit)
        { return child.getContent();
        }
      }
      return buf.toString();
    }
    return null;
    
  }  
  
  public boolean containsMarkup()
  {
    if (children!=null)
    { 
      for (TglUnit child:children)
      { 
        if (!(child instanceof ContentUnit))
        { return true;
        }
      }
    }
    return false;
  }
  
  public void addProperty
    (PropertyUnit propertyUnit)
  {
    if (properties==null)
    { properties=new PropertyUnit[0];
    }
    properties=ArrayUtil
      .append(properties,propertyUnit);
    if (debug)
    { log.fine("Added property "+propertyUnit.getPropertyName());
    }
  }
  
  /**
   * <p>Create a tree of Elements bound into an application context
   *   (the Assembly) which implements the functional behavior 
   *   specified by the TGL document.
   * </p>
   */
  @Override
  public Component bind(Focus<?> focus,Parent parentElement)
    throws ContextualException
  { 
    if (debug)
    { log.debug(getPosition().toURI()+": Binding...");
    }
    try
    {
      if (contextX!=null)
      { focus=bindContext(focus,null,null,getNamespaceResolver());
      }
      return bind(focus,parentElement,createElement());
    }
    finally
    {
      if (debug)
      { log.debug(getPosition().toURI()+": Bound...");
      }
    }
  }

  protected Element createElement()
  { return new DefaultElement();
  }
  
  /**
   * Extend this Unit by applying the specified set of attributes and using the
   *   supplied children as content instead of this Unit's own children.
   * 
   * @param attribs
   * @param focus
   * @param parentElement
   * @param children
   * @return
   * @throws MarkupException
   */
  public Component bindExtension
    (Attribute[] attribs
    ,Focus<?> focus
    ,Parent parentElement
    ,List<TglUnit> children
    ,PrefixResolver attributePrefixResolver
    )
    throws ContextualException
  { 
    if (contextX!=null)
    { 
      // TODO: Replace/augment contextX with "tgconst:varname=''" and
      //   dynamically build struct using passed arguments.
      URI[] aliasesA=
        new URI[] 
          {URIUtil.removePathSuffix(getPosition().getContextURI(),".tgl")
            ,referencedURI
          };
      if (aliases!=null)
      { 
        aliasesA
          =ArrayUtil.concat
            (aliasesA, 
              aliases.toArray(new URI[aliases.size()])
            );
      }
      focus=bindContext
        (focus
        ,attribs
        ,aliasesA
        ,attributePrefixResolver
        );
    }
    else if (attribs!=null && attribs.length>0)
    { throw new MarkupException("Unrecognized attribute "+attribs[0].getName(),getPosition());
    }
    if (children!=null && children.size()>0)
    { 
      // XXX Need to track the overlay children so they can be inserted
      
    }
    return bind(focus,parentElement,createElement());
  }

  /**
   * Applies a set of parameter values supplied via TGL tag attributes to 
   *   the constant instantiation context of this element.
   * 
   * @param focus
   * @param attribs
   * @param contextX
   * @param contextAlias
   * @return A new Focus which exposes the same subject as the input and
   *   adds the contextX as a Facet
   * @throws ContextualException
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected Focus<?> bindContext
    (Focus<?> focus
    ,Attribute[] attribs
    ,URI[] contextAlias
    ,PrefixResolver attributePrefixResolver
    )
    throws ContextualException
  {
    NamespaceContext.push(getNamespaceResolver());
    try
    {
      Channel<?> context;
      try
      { context=new ConstantChannel(focus.bind(contextX));
      }
      catch (BindException x)
      { 
        throw new ContextualException
          ("Error binding context expression for define",x);
      }
      Focus<?> contextFocus=focus.chain(context);
      if (contextAlias!=null)
      { 
        for (URI uri:contextAlias)
        { 
          if (uri!=null)
          { contextFocus.addAlias(uri);
          }
        }
      }
      focus=focus.chain(focus.getSubject()); 
      focus.addFacet(contextFocus);
      
      if (attribs!=null)
      {
        for (Attribute attrib:attribs)
        { 
          String name=attrib.getName();
          NamespaceContext.push(attributePrefixResolver);
          try
          {
            if (name.startsWith("$"))
            { 
              Channel source=focus.bind(Expression.create(attrib.getValue()));
              if (!source.isConstant())
              { 
                throw new ContextualException
                  ("Attribute "+attrib.getName()
                  +" expression `"+attrib.getValue()+"` is not constant, and"
                  +" cannot be used for bind-time context"
                  );
              }
              
              Setter setter
                =new Setter
                  (source
                  ,contextFocus.bind(Expression.create(name.substring(1)))
                  );
              if (!setter.set())
              {
                throw new ContextualException
                  ("Attribute "+attrib.getName()
                  +" could not be applied"
                  );
              }
              
            }
            else
            {
              DictionaryBinding attribBinding
                =new DictionaryBinding(attrib.getName());
              attribBinding.bind(contextFocus);
              attribBinding.set(attrib.getValue());
            }
          }
          catch (BindException x)
          {
            throw new ContextualException
              ("Error binding context expression for define",getPosition(),x);
          }
          catch (spiralcraft.lang.ParseException x)
          {
            throw new ContextualException
              ("Error in attribute name",getPosition(),x);
          }
          finally
          { NamespaceContext.pop();
          }
          
        }
      }
      if (context.get() instanceof Struct)
      { ((Struct) context.get()).freeze();
      }
      return focus;
    }
    finally
    { NamespaceContext.pop();
    }
  }


  public void dumpTree(PrintWriter writer,String linePrefix)
  { 
    writer.println(linePrefix+toString());
    for (TglUnit unit: children)
    { unit.dumpTree(writer,linePrefix+"  ");
    }
  }  
  
  protected Component defaultBind(Focus<?> focus,Element parentElement)
    throws ContextualException
  { return bind(focus,parentElement,new DefaultElement());
  }
  
  
  protected Component bind
    (Focus<?> focus
    ,Parent parentElement
    ,Component unboundElement
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
        ,URIPool.create(attrib.getValue())
        );
      return true;
    }
    else if (attrib.getName().equals("tgns"))
    {
      mapNamespace
        (""
        ,URIPool.create(attrib.getValue())
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
    { 
      debug=Boolean.parseBoolean(value);
      if (debug)
      { logLevel=Level.DEBUG;
      }
    }
    else if (name.equals("logLevel"))
    { 
      logLevel=Level.valueOf(value);
      if (logLevel==null)
      { throw new ParseException("Invalid logLevel ["+value+"]",getPosition());
      }
      debug=logLevel.isDebug();
    }
    else if (name.equals("import"))
    { this.includeResource(value);
    }
    else if (name.equals("contextX"))
    { 
      try
      { this.contextX=Expression.parse(value);
      }
      catch (spiralcraft.lang.ParseException x)
      { 
        throw new ParseException
          ("Error parsing context expression "+value,getPosition(),x);
      }
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
  { return trim!=null?trim:(parent!=null?parent.getTrim():false);
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
    else if (namespaceRoot)
    { 
      initPrefixResolver();
      return prefixResolver;
    }
    else if (parent!=null)
    { return parent.getNamespaceResolver();
    }
    return null;
  }  
  
  @Override
  public void close()
    throws MarkupException
  {
    if (children!=null && children.size()>0 && getTrim())
    {
      if (children.get(0) instanceof ContentUnit)
      { ((ContentUnit) children.get(0)).trimStart();
      }
      
      if (children.size()>1 
          && (children.get(children.size()-1) instanceof ContentUnit)
          )
      { ((ContentUnit) children.get(children.size()-1)).trimEnd();
      }
    }
    super.close();
  }
  
  protected DocletUnit includeResource(String qname)
    throws MarkupException
  {

    URI resourceURI=null;
    if (qname.startsWith(":"))
    { 
      qname=qname.substring(1);
    
      ContextualName cname=null;
      try
      { 
        cname=new ContextualName
          (qname
          ,new StandardPrefixResolver(NamespaceContext.getPrefixResolver())
            {
              { 
                this.mapPrefix
                  (""
                  ,URIPool.get
                    (TglUnit.this.getPosition().getContextURI().resolve(".")
                      .normalize()
                    )
                  );
              }
            }
          );
      }
      catch (UnresolvedPrefixException x)
      {  
        throw new MarkupException
          ("Error resolving ["+qname+"]",getPosition(),x);
      }
  
    
      resourceURI=cname.getQName().toURIPath();
    }
    else
    { resourceURI=URIPool.create(qname);
    }

    if (!resourceURI.isAbsolute())
    {
      DocletUnit parentDoc=findUnit(DocletUnit.class);
      URI baseURI=parentDoc.getSourceURI();
      resourceURI=URIPool.get(baseURI.resolve(resourceURI));

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
      ("Error including '"+resourceURI+"':"+x
        ,getPosition()
        ,x
      );
    }
    catch (IOException x)
    {
      throw new MarkupException
      ("Error including URI '"+resourceURI+"':"+x
        ,getPosition()
        ,x
      );
    }
  }
  
  
  
  
}

class DefaultElement
  extends Element
{

}
