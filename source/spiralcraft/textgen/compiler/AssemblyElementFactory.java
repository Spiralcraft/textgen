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
import spiralcraft.lang.Focus;
//import spiralcraft.log.ClassLogger;
import spiralcraft.text.markup.MarkupException;

import spiralcraft.text.ParsePosition;

import spiralcraft.textgen.Element;

import spiralcraft.text.xml.Attribute;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.net.URI;

import java.util.LinkedHashMap;

import org.xml.sax.SAXException;

public class AssemblyElementFactory
  implements ElementFactory
{

//  private static final ClassLogger log
//    =ClassLogger.getInstance(AssemblyElementFactory.class);
  
  private final AssemblyClass assemblyClass;
  private final ParsePosition position;
//  private URI namespaceUri;
  private String elementClassName;
  private LinkedHashMap<String,PropertyUnit> properties;
  
  public AssemblyElementFactory
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
      for (PropertyUnit unit: properties)
      {
        
        Attribute nature=unit.getAttribute("nature");
        
        StringBuilder buf=new StringBuilder();
        PropertySpecifier property
          =new PropertySpecifier
          (assemblyClass
          ,unit.getPropertyName()
          );
        
        if (unit.getChildren()!=null)
        {
          for (TglUnit child : unit.getChildren())
          { 
            if (child instanceof ElementUnit)
            { 
//              Expression<?> expression=((ElementUnit) child).getExpression();
//              if (expression!=null)
//              { property.setExpression(expression.getText());
//              }
//              else
//              {
                throw new MarkupException
                  ("Unsupported use of a child Element in a property Element"
                  ,child.getPosition()
                  );
//              }
              // Translate the contents into a property def.
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
          if (buf.length()>0)
          { property.addCharacters(buf.toString().toCharArray());
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
  
  
  @SuppressWarnings("unchecked")
  public Element createElement(Element parentElement)
    throws MarkupException
  {
    // Assembly<?> parentAssembly=parentElement.getAssembly();
    Focus<?> parentFocus=parentElement.getFocus();
    
    try
    { 
      Assembly<?> assembly=assemblyClass.newInstance(parentFocus);
      Element element=(Element) assembly.get();    
      element.setAssembly(assembly);
      element.setParent(parentElement);
      
      if (properties!=null)
      {
        for (PropertyUnit unit: properties.values())
        {
          Attribute nature=unit.getAttribute("nature");
          if ("bean".equals(nature.getValue()))
          { 
            StringBuilder buf=new StringBuilder();
            for (TglUnit child : unit.getChildren())
            { 
              if (child instanceof ElementUnit)
              { 
                throw new MarkupException
                  ("Unsupported use of a child Element in a property Element"
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
                
              Object data
                =new DataReader().readFromInputStream
                  (new ByteArrayInputStream(buf.toString().getBytes())
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
      }
      
      return element;
    }
    catch (BuildException x)
    { 
      throw new MarkupException
        ("Error instantiating Element: "+x.toString(),position,x);
    }
    

  }
}
