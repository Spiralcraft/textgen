package spiralcraft.textgen.compiler;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.BuildException;
import spiralcraft.builder.PropertySpecifier;

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

//import spiralcraft.log.ClassLogger;
import spiralcraft.text.markup.MarkupException;

import spiralcraft.text.ParseException;
import spiralcraft.text.ParsePosition;

import spiralcraft.textgen.Element;

import spiralcraft.text.xml.Attribute;
import spiralcraft.util.ContextDictionary;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.net.URI;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.xml.sax.SAXException;

public class ElementFactory
{

//  private static final ClassLogger log
//    =ClassLogger.getInstance(AssemblyElementFactory.class);
  
  private final AssemblyClass assemblyClass;
  private final ParsePosition position;
//  private URI namespaceUri;
  private String elementClassName;
  private LinkedHashMap<String,PropertyUnit> properties;
  
  public ElementFactory
    (URI namespaceUri
    ,String elementName
    ,Attribute[] attributes
    ,PropertyUnit[] properties
    ,ParsePosition position
    )
    throws MarkupException
  {
    elementClassName=elementName;
    this.position=position.clone();
//    this.namespaceUri=namespaceUri;
    
    assemblyClass=new AssemblyClass
      (position.getContextURI()
        ,namespaceUri
        ,elementClassName
        ,null
        ,null
      );

    if (attributes!=null)
    { 
      for (int i=0;i<attributes.length;i++)
      { 
        assemblyClass.addPropertySpecifier
          (new PropertySpecifier
            (assemblyClass
                ,attributes[i].getName()
                ,attributes[i].getValue()
            )
          );
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
          (namespaceUri+elementClassName+" does not resolve to an" +
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
  
  public AssemblyClass getAssemblyClass()
  { return assemblyClass;
  }

  private void handlePropertyUnit(PropertyUnit unit)
  {
        
    Attribute nature=unit.getAttribute("nature");
        
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
      if (objectUnits.size()>0)
      { 
        // This is the quick way- build an assembly tree from the
        //   property tree and instantiate that.
        PropertySpecifier property
          =new PropertySpecifier
          (assemblyClass
          ,unit.getPropertyName()
          );        
        for (ElementUnit objectUnit:objectUnits)
        { property.addAssemblyClass(objectUnit.getAssemblyClass());
        }
        assemblyClass.addPropertySpecifier(property);
        
      }
      else if (buf.length()>0)
      { 
        // Add immediately
        PropertySpecifier property
          =new PropertySpecifier
          (assemblyClass
          ,unit.getPropertyName()
          );        
        property.addCharacters(buf.toString().toCharArray());
        assemblyClass.addPropertySpecifier(property);
      }
    }
    else if (nature.getValue().equals("bean"))
    { 
      if (this.properties==null)
      { this.properties=new LinkedHashMap<String,PropertyUnit>();
      }
      this.properties.put(unit.getPropertyName(),unit);
    }
  }
  
  
  @SuppressWarnings("unchecked")
  public Element createElement(Element parentElement)
    throws MarkupException
  {
    
    try
    { 
      Assembly assembly
        =assemblyClass.newInstance(parentElement.getFocus());
      
      Element element;
      Object object=assembly.get();
      if (object instanceof Element)
      { element=(Element) object;
      }
      else
      { 
        if (parentElement instanceof PropertyElement)
        { element=new ObjectElement(object);
        }
        else
        { 
          throw new MarkupException
            ("Only a subtype of spiralcraft.textgen.Element is allowed here"
            ,position
            );
        }
      }
      
      
      element.setCodePosition(position);
      element.setAssembly(assembly);
      element.setParent(parentElement);
        
      
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
      
      return element;
    }
    catch (BuildException x)
    { 
      throw new MarkupException
        ("Error instantiating Element: "+x.toString(),position,x);
    }
    

  }
  
  
  @SuppressWarnings("unchecked")
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
