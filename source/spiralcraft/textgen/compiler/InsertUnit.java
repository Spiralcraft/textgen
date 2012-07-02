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


import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;
import spiralcraft.text.ParseException;
import spiralcraft.text.markup.MarkupException;

import spiralcraft.textgen.Element;

import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.Parent;
import spiralcraft.common.ContextualException;
import spiralcraft.common.namespace.NamespaceContext;
import spiralcraft.common.namespace.QName;


import spiralcraft.text.xml.Attribute;
import spiralcraft.util.URIUtil;
import spiralcraft.vfs.Package;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;
/**
 * A Unit which inserts the contents of an ancestral IncludeUnit, or a unit referenced by a defined name
 */
public class InsertUnit
  extends ProcessingUnit
{
  private static final ClassLog log
    =ClassLog.getInstance(InsertUnit.class);
  

  private String referencedName;
  private QName qName;
  private String tagName;
  private boolean require=false;
  private Attribute[] attributes;
  private URI resourceURI;
  private Resource resource;
  
  public InsertUnit
    (TglUnit parent
    ,TglCompiler<?> compiler
    ,Attribute[] attribs
    ,String tagName
    )
    throws MarkupException,ParseException
  { 
    super(parent,compiler);
    allowsChildren=true;
    this.tagName=tagName;
    
    
    if (tagName.startsWith("@"))
    {
      // Form <%@insert ...
      for (Attribute attrib: attribs)
      {
        if (attrib.getName().equals("name"))
        { this.referencedName=attrib.getValue();
        }
        else if (attrib.getName().equals("require"))
        { require=Boolean.parseBoolean(attrib.getValue());
        }
        else if (super.checkUnitAttribute(attrib))
        {
        }
        else
        { 
          throw new MarkupException
            ("Attribute '"+attrib.getName()+"' not in {name,require}"
            ,compiler.getPosition()
            );
        }
      }
    }
    else
    {

      
      this.require=true;

      ArrayList<Attribute> otherAttribs=new ArrayList<Attribute>();
      for (Attribute attrib: attribs)
      {
        if (attrib.getName().equals("require"))
        { require=Boolean.parseBoolean(attrib.getValue());
        }
        else if (!super.checkUnitAttribute(attrib))
        { otherAttribs.add(attrib);
        }
      }
      this.attributes=otherAttribs.toArray(new Attribute[otherAttribs.size()]);
      
      // Form <%refname ...
      if (this.referencedName==null)
      {
        NamespaceContext.push(getNamespaceResolver());
        try
        {
          this.qName
            =resolvePrefixedName
              (tagName,getPosition().getContextURI().resolve(".").normalize());
         
          this.referencedName
            =resolvePrefixedName
              (tagName,getNamespaceResolver().resolvePrefix("")).toString();
          
          this.resourceURI=URIUtil.addPathSuffix(qName.toURIPath(),".tgl");
          defineReferencedResource();
        }
        catch (StackOverflowError x)
        { throw new MarkupException("Recursive definition of "+referencedName,getPosition());
        }
        finally
        { NamespaceContext.pop();
        }
        
      }
    }   
  }
  
  
  @Override
  public String getName()
  { return tagName;
  }
  
  /**
   * Binds this element's content
   * 
   * @param focus
   * @param parentElement
   * @return
   */
  private InsertElement bindDefaultContent(Focus<?> focus,Parent parentElement)
    throws ContextualException
  { return (InsertElement) bind(focus,parentElement,new InsertElement());
  }
  
  
  private void defineReferencedResource()
    throws ParseException,MarkupException
  {
    if (qName!=null && findDefinition(referencedName)==null)
    { 
      if (resourceURI.getScheme()!=null 
          && !Resolver.getInstance().handlesScheme(resourceURI.getScheme())
          )
      { return;
      }
      
      try
      { 
         
        resource
          =Resolver.getInstance().resolve(resourceURI);
        if (!resource.exists())
        {          
          Package pkg=Package.fromContainer(resource.getParent());
          if (pkg!=null)
          { resource=pkg.searchForBaseResource(resource);
          }
        }
        
        if (resource!=null && resource.exists())
        {
          try
          {
            DocletUnit referencedUnit
              =compiler.subCompile
                (this
                ,resource.getURI()
                );
            referencedUnit.setReferencedURI(qName.toURIPath());
            this.define(referencedName,referencedUnit);
          }
          catch (IOException e)
          { throw new ParseException("Error reading ["+qName+"]",getPosition(),e);
          }            
        }
      }
      catch (ContextualException x)
      { throw new MarkupException("Error reading theme for "+resourceURI,this.getPosition(),x);
      }
      catch (IOException x)
      { throw new MarkupException("Error compiling "+resourceURI,this.getPosition(),x);
      }

    }
  }
  
  @Override
  public Element bind(Focus<?> focus,Parent parentElement)
    throws ContextualException
  {
    if (referencedName!=null)
    {
      // This is a directive to insert a named reference to another component
      
      TglUnit referencedUnit;
      
      try
      { referencedUnit=findDefinition(referencedName);
      }
      catch (StackOverflowError x)
      {
        throw new ContextualException
          ("Recursive definition of "+referencedName,getPosition());
      }
      
      Exception resolveException=null;
      

      
      if (referencedUnit!=null)
      { 
        if (debug)
        { log.fine("Binding referenced unit '"+referencedName+"'");
        }
        referencedUnit.exportDefines(this);
        return referencedUnit.bindExtension
          (attributes,focus,parentElement,children,getNamespaceResolver());
      }
      else if (!require)
      { 
        if (debug)
        { log.fine("Binding default for '"+referencedName+"'");
        }
        // Render default self contents
        
        try
        { return bindDefaultContent(focus,parentElement);
        }
        catch (BindException x)
        { throw new MarkupException(x.toString(),getPosition(),x);
        }
        
      }
      else
      { 
        throw new MarkupException
          ("Textgen definition for ["+referencedName+"] not found. "
          +(resourceURI!=null
            ?(resolveException!=null
              ?("Resolving ["+resourceURI+"]: "+resolveException)
              :("and resource ["
                +(resource!=null
                  ?resource.getURI()
                  :resourceURI
                )
                +"] not found")
              )
            :""
            )
          ,getPosition()
          );
      }
    }
    else
    {
      // If the insert is contained in a Define unit in this document,
      //   then it should render the children of the containing "insert"
      //   reference.
      Element element=null;
      DefineUnit defineUnit=this.findUnitInDocument(DefineUnit.class);
      
      if (defineUnit!=null)
      { element=new InsertIncludeElement(defineUnit);
      }
      else
      { 
        TglUnit docletRoot=this.findUnitInDocument(DocletUnit.class);
        if (logLevel.isFine())
        {
          log.fine("Root is "+docletRoot
                +(docletRoot!=null?" parent is "+docletRoot.getParent():"")
                );
        }
        if (docletRoot!=null && (docletRoot.getParent() instanceof InsertUnit))
        { 
          // XXX This document may be defined at a generic place- we
          //   need to find the referencing insert, not the defining
          //   insert.
          //     We really need to traverse the Element tree and
          //   not the Unit tree to find who really inserted us
          //   e.g. find the Element for the DocletRoot and it's 
          //   Parent element's unit will be the correct Insert
          element=new InsertOverlayElement((InsertUnit) docletRoot.getParent());
        }
        else
        { element=new InsertIncludeElement();
        }
        
      }
      return bind(focus,parentElement,element);
    }
  }
  
  
}

class InsertElement
  extends Element
{
  

}


class InsertIncludeElement
  extends Element
{
  private IncludeElement ancestorInclude;
  private DefineUnit defineUnit;
  private DefineElement ancestorDefine;
  
  public InsertIncludeElement()
  { super();
  }
  
  public InsertIncludeElement(DefineUnit defineUnit)
  { this.defineUnit=defineUnit;
  }  

  
  @Override
  protected Focus<?> bindStandard(Focus<?> focus)
    throws ContextualException
  { 
    
    List<TglUnit> children=getScaffold().getChildren();
    if (defineUnit!=null)
    {
      // If the anonymous insert is inside a define unit, bind the default
      //   children of the insert that referenced the define.
        
      // Find the insert that referenced our define
      ancestorDefine=defineUnit.findBoundElement(getParent());
    }
    
    if (ancestorDefine!=null)
    { children=ancestorDefine.getOverlay();
    }
    else
    {
      // Get the nearest containing Include that is not in the same 
      //   document.
    
      Element containingDocument
        =findComponent(DocletUnit.RootElement.class);
    
      if (containingDocument!=null)
      { ancestorInclude=containingDocument.findComponent(IncludeElement.class);
      }
    
    }

    super.bindChildren(focus,children);
    return focus;
  }
  
  @Override
  protected void messageStandard
    (Dispatcher context
    ,Message message
    )
  {    
    if (ancestorInclude!=null)
    { ancestorInclude.messageClosure(context,message);
    }
    else 
    { super.messageStandard(context, message);
    }


  }
}
  
class InsertOverlayElement
  extends Element
{
  private InsertUnit includingInsertUnit;
  
  public InsertOverlayElement()
  { super();
  }
  
  public InsertOverlayElement(InsertUnit insertUnit)
  { this.includingInsertUnit=insertUnit;
  } 
  
  @Override
  protected Focus<?> bindStandard(Focus<?> focus)
    throws ContextualException
  { 
    
    List<TglUnit> children=getScaffold().getChildren();
    if (includingInsertUnit!=null)
    { 
      children=includingInsertUnit.getChildren();
      List<TglUnit> newChildren=new ArrayList<TglUnit>(children.size()-1);
      for (TglUnit child:children)
      { 
        if (!(child instanceof DefineUnit) && !(child instanceof DocletUnit))
        { newChildren.add(child);
        }
      }
      children=newChildren;
    }

    super.bindChildren(focus,children);
    return focus;
  }
  
}