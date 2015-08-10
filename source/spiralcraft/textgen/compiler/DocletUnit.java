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

import spiralcraft.common.declare.DeclarationInfo;
import spiralcraft.common.namespace.NamespaceContext;
import spiralcraft.lang.Expression;
import spiralcraft.textgen.Element;
import spiralcraft.util.URIUtil;
import spiralcraft.util.refpool.URIPool;
import spiralcraft.text.ParseException;
import spiralcraft.text.markup.MarkupException;
import spiralcraft.text.markup.Unit;
import spiralcraft.text.xml.Attribute;

import java.io.IOException;
import java.net.URI;

import spiralcraft.vfs.Resource;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * A compilation unit (ie. a file or other container) of tgl markup.
 * 
 */
public class DocletUnit
  extends TglUnit
{
  protected final Resource resource;
  private final ArrayList<DocletUnit> subDocs
    =new ArrayList<DocletUnit>();
  private LinkedList<ElementUnit> elements
    =new LinkedList<ElementUnit>();
  
  { namespaceRoot=true;
  }
  
  public DocletUnit(TglUnit parent,Resource resource,TglCompiler<?> compiler)
  { 
    super(parent,compiler);
    this.resource=resource;
    initPrefixResolver();
    addAlias(URIPool.create("class:/spiralcraft/textgen/elements/doclet"));
    
    if (parent!=null)
    {
      DocletUnit parentDoc=parent.findUnit(DocletUnit.class);
      if (parentDoc!=null)
      { parentDoc.registerSubDoclet(this);
      }
    }
    
  }
  
  public void setAttributes(Attribute[] attribs)
    throws MarkupException,ParseException
  { 
    NamespaceContext.push(getNamespaceResolver());
    try
    {
    
      // Form <%@insert ...
      for (Attribute attrib: attribs)
      {
  
        if (super.checkUnitAttribute(attrib))
        {
        }
        else if (attrib.getName().equals("package"))
        { 
          try
          { 
            addAlias
              (URIUtil.addPathSegment
                (URIPool.create(attrib.getValue())
                ,getLocalName()
                )
              );
          }
          catch (RuntimeException x)
          {
            throw new ParseException
              ("Error parsing package URI",getPosition(),x);
          }
        }
        else if (attrib.getName().equals("contextX"))
        { 
          try
          { this.contextX=Expression.parse(attrib.getValue());
          }
          catch (spiralcraft.lang.ParseException x)
          { 
            throw new ParseException
              ("Error parsing contextX expression",getPosition(),x);
          }
        }
        else if (attrib.getName().equals("debug"))
        { this.debug="true".equals(attrib.getValue());
        }
        else
        { 
          throw new MarkupException
            ("Attribute '"+attrib.getName()+"' not in {package,contextX,debug}"
            ,compiler.getPosition()
            );
        }
      }   
    }
    finally
    { NamespaceContext.pop();
    }
  }
  
  @Override
  public String getName()
  { return "__doclet";
  }
  
  public String getLocalName()
  { return URIUtil.unencodedLocalName
      (URIUtil.removePathSuffix
        (resource.getURI()
        ,".tgl"
        )
      );
  }
  
  public URI getSourceURI()
  { return resource.getURI();
  }
  
  void registerSubDoclet(DocletUnit subDoc)
  { subDocs.add(subDoc);
  }
  
  @SuppressWarnings({"unchecked","rawtypes"}) // Downcast from runtime check
  @Override
  public Unit findUnit(Class clazz)
  {
    if (clazz==NamespaceUnit.class)
    { 
      // Stop resolving namespaces within the document
      return null;
    }
    else
    { return super.findUnit(clazz);
    }
  }
  
  /**
   * @return The modification time of the most recently modified resource
   *   in the tree.
   */
  public long getLastModified()
    throws IOException
  { 
    long time=resource.getLastModified();
    for (DocletUnit subDoc:subDocs)
    { time=Math.max(time,subDoc.getLastModified());
    }
    if (logLevel.isDebug())
    { log.debug(resource+" lastModified="+resource.getLastModified()+" / "+time);
    }
    return time;
  }
  
  void registerElement(ElementUnit elementUnit)
  { elements.add(elementUnit);
  }
  
  public boolean hasStaleElements()
  {
    for (ElementUnit element:elements)
    { 
      if (element.getAssemblyClass().isStale())
      { return true;
      }
    }
    return false;
  }
  
  
  /**
   * Finds a unit that is an ancestor in the containership hierarchy within
   *   the scope of the current document.
   * 
   * @param unitClass
   * @return
   */
  @SuppressWarnings("unchecked")
  @Override
  public <X extends TglUnit> X findUnitInDocument(Class<X> unitClass)
  { 
    if (unitClass.isAssignableFrom(getClass()))
    { return (X) this;
    }
    else return null;
  }
  
  @Override
  public Element createElement()
  { return new RootElement();
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
  @Override
  public boolean getTrim()
  { return trim!=null?trim:false;
  }
    
  class RootElement
    extends Element
  {
    { 
      setDebug(DocletUnit.this.debug);
      setDeclarationInfo
        (new DeclarationInfo(null,null,DocletUnit.this.getSourceURI())
        );
    }
    
    @Override
    public URI getContextURI()
    { return resource.getURI();
    }
  }
  
}
