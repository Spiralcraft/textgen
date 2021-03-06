package spiralcraft.textgen.compiler;

import spiralcraft.app.Component;
import spiralcraft.app.Parent;
import spiralcraft.builder.Assembly;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.BuildException;
import spiralcraft.builder.PropertySpecifier;
import spiralcraft.common.declare.DeclarationInfo;
import spiralcraft.common.namespace.StandardPrefixResolver;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.data.sax.DataReader;
import spiralcraft.data.util.StaticInstanceResolver;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;

import spiralcraft.log.ClassLog;
import spiralcraft.text.markup.MarkupException;

import spiralcraft.text.ParseException;
import spiralcraft.text.ParsePosition;
import spiralcraft.text.Renderer;
import spiralcraft.text.Wrapper;

import spiralcraft.textgen.Element;

import spiralcraft.text.xml.Attribute;
import spiralcraft.util.ContextDictionary;
import spiralcraft.util.refpool.URIPool;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.net.URI;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.xml.sax.SAXException;

public class ElementFactory
{

  private static final ClassLog log
    =ClassLog.getInstance(ElementFactory.class);
  
  private final AssemblyClass assemblyClass;
  private final ParsePosition position;
  private URI namespaceURI;
  private String elementClassName;
  private LinkedHashMap<String,PropertyUnit> properties;
  private boolean debug;
  
  public ElementFactory
    (URI namespaceURI
    ,String elementName
    ,Attribute[] attributes
    ,PropertyUnit[] properties
    ,ParsePosition position
    ,StandardPrefixResolver prefixResolver
    ,ElementUnit elementUnit
    )
    throws MarkupException
  {
    debug=elementUnit.debug;
    elementClassName=elementName;
    this.position=position.clone();
    this.namespaceURI=namespaceURI;
    
    assemblyClass=new AssemblyClass
      (position.getContextURI()
        ,null
        ,namespaceURI
        ,elementClassName
        ,null
        ,null
      );

    
    if (attributes!=null)
    { 
      for (int i=0;i<attributes.length;i++)
      { 
        
        PropertySpecifier specifier;
        String name=attributes[i].getName();
        
        if (name.startsWith("$"))
        {
          specifier=new PropertySpecifier
            (assemblyClass
              ,name.substring(1)
            );
          specifier.setExpression(attributes[i].getValue());
        }
        else
        {
          specifier=new PropertySpecifier
            (assemblyClass
              ,attributes[i].getName()
              ,attributes[i].getValue()
            );
        }
        specifier.setPrefixResolver(prefixResolver);
        if (debug)
        { log.fine("Adding property "+specifier+" to "+namespaceURI+"/"+elementClassName);
        }
        assemblyClass.addPropertySpecifier
          (specifier);
      }
    }
    
    if (properties!=null)
    { 
      for (PropertyUnit unit:properties)
      { handlePropertyUnit(unit);
      }
    }
    
    try
    { assemblyClass.resolve();
    }
    catch (BuildException x)
    { 
      if  (x.getCause() instanceof ClassNotFoundException)
      { 
        throw new MarkupException
          (namespaceURI+elementClassName+" does not resolve to an" +
           " Assembly or a Class."
          ,position
          ,x.getCause()
          );
      }
      else
      { throw new MarkupException(position,x);
      }
    }
    
  }
  
  public void setInstanceX(String instanceX)
  { assemblyClass.setInstanceX(instanceX);
  }
  
  public AssemblyClass getAssemblyClass()
  { return assemblyClass;
  }

  private void handlePropertyUnit(PropertyUnit unit)
    throws MarkupException
  {
    Attribute nature=null;
    Attribute expression=null;
    
    for (Attribute attrib:unit.getAttributes())
    { 
      if (attrib.getName().startsWith("tgns:")
          || attrib.getName().startsWith("textgen:")
          )
      { // ignore unit attribs, they've been processed
      }
      else if (attrib.getName().equals("nature"))
      { nature=attrib;
      }
      else if (attrib.getName().equals("x"))
      { expression=attrib;
      }
      else
      { 
        throw new MarkupException
          ("Unrecognized attribute "
           +"'"+attrib.getName()+"' in property specifier '"
           +unit.getName()+"'. Use one of [nature,x]"
          ,position
          );
      }
      
    }
    
        
    StringBuilder buf=new StringBuilder();
    List<ElementUnit> objectUnits=new ArrayList<ElementUnit>();

        
    if (unit.getChildren()!=null)
    {
      for (TglUnit child : unit.getChildren())
      { 
        
        if (child instanceof ElementUnit)
        { 
          objectUnits.add((ElementUnit) child);
          
        }
        else if (child instanceof ContentUnit)
        {
          buf.append
            (((ContentUnit) child)
            .getContent().toString());
            
        }
      }
    } 
        
    if (nature==null)
    {
      PropertySpecifier property
        =new PropertySpecifier
        (assemblyClass
        ,unit.getPropertyName()
        );      

      property.setPrefixResolver
        ((StandardPrefixResolver) unit.getNamespaceResolver());
      boolean set=false;
      if (objectUnits.size()>0)
      { 
        // This is the quick way- build an assembly tree from the
        //   property tree and instantiate that.
        
        for (ElementUnit objectUnit:objectUnits)
        { property.addAssemblyClass(objectUnit.getAssemblyClass());
        }
        set=true;
      }
      
      if (expression!=null)
      { 
        if (objectUnits.size()>0)
        { 
          throw new MarkupException
            ("Property specifier with expression cannot also have contents"
            ,position
            );
        }
        property.setExpression(expression.getValue());
        set=true;
      }
      
      if (!set && buf.length()>0)
      {        
        property.addCharacters(buf.toString().toCharArray());
      }

      if (debug)
      { log.fine("Adding property "+property+" to "+namespaceURI+"/"+elementClassName);
      }

      assemblyClass.addPropertySpecifier(property);
      
    }
    else if (nature.getValue().equals("bean"))
    { 
      if (this.properties==null)
      { this.properties=new LinkedHashMap<String,PropertyUnit>();
      }
      this.properties.put(unit.getPropertyName(),unit);
    }
  }
  
  
  @SuppressWarnings({"rawtypes"})
  public Component createElement(Focus<?> focus,Parent parentElement)
    throws MarkupException
  {
    
    try
    { 
      Assembly assembly
        =assemblyClass.newInstance(focus);
      
      Component component;
      Object object=assembly.get();
      
      
      if (object instanceof Component)
      { component=(Component) object;
      }
      else if (object instanceof Renderer)
      { component=new RendererElement((Renderer) object);
      }
      else if (object instanceof Wrapper)
      { component=new WrapperElement((Wrapper) object);
      }
      else
      { 
        if (parentElement instanceof PropertyElement)
        { component=new ObjectElement(object);
        }
        else
        { 
          // Put the instance directly into the Chain
          component=new FocusChainElement(object);
        }
      }
      
      URI focusURI=URIPool.create(namespaceURI.toString()+elementClassName);
      
      if (component instanceof Element)
      {
        Element element=(Element) component;
        element.setCodePosition(position);
        element.setAssembly(assembly);
        element.setFocusURI(focusURI);
      }
      assembly.getFocus().addAlias(focusURI);
      component.setDeclarationInfo
        (new DeclarationInfo(component.getDeclarationInfo(),focusURI, position.toURI()));
      
      if (properties!=null)
      {
        for (PropertyUnit unit: properties.values())
        {
          Attribute nature=unit.getAttribute("nature");
          if ("bean".equals(nature.getValue()))
          { readBean(assembly,unit);
          }
        }
      }
      
      return component;
    }
    catch (BuildException x)
    { 
      throw new MarkupException
        ("Error instantiating Element: "+x.toString(),position,x);
    }
    

  }
  
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void readBean(Assembly assembly,PropertyUnit unit)
    throws MarkupException
  {
    StringBuilder buf=new StringBuilder();
    for (TglUnit child : unit.getChildren())
    { 
      if (child instanceof ElementUnit)
      { 
        throw new MarkupException
          ("Unsupported use of a child Element in a property bean Element"
          ,child.getPosition()
          );
      }
      else if (child instanceof ContentUnit)
      {
        buf.append
          (((ContentUnit) child)
          .getContent().toString());
      }
            
    }
    // Translate the contents into a property def.
          
    try
    {
      Channel target
        =assembly.getFocus().bind
          (Expression.create(unit.getPropertyName()));
            
      Class<?> clazz=target.getContentType();
      Type<?> type=Type.resolve(ReflectionType.canonicalURI(clazz));
                
      String xml=buf.toString();
      try
      { xml=ContextDictionary.substitute(xml);
      }
      catch (ParseException x)
      { throw new MarkupException(unit.getPosition(),x);
      }
      Object data
        =new DataReader().readFromInputStream
          (new ByteArrayInputStream(xml.getBytes())
          ,type
          ,position.getContextURI()
          );
      if (data instanceof DataComposite)
      { 
        target.set
          (type.fromData
            ((DataComposite) data
            ,new StaticInstanceResolver(target.get())
            )
          );
      }
      
    }
    catch (AccessException x)
    { 
      throw new MarkupException
        ("Error binding property element",position,x);
    }
    catch (BindException x)
    { 
      throw new MarkupException
        ("Error binding property element",position,x);
    }
    catch (SAXException x)
    { 
      throw new MarkupException
        ("Error binding property element",position,x);
    }
    catch (DataException x)
    { 
      throw new MarkupException
        ("Error binding property element",position,x);
    }
    catch (IOException x)
    { 
      throw new MarkupException
        ("Error binding property element",position,x);
    }
  }
  
}
