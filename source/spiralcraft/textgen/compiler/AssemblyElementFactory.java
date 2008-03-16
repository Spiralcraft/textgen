package spiralcraft.textgen.compiler;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.AssemblyLoader;
import spiralcraft.builder.BuildException;
import spiralcraft.builder.PropertySpecifier;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLogger;
import spiralcraft.text.markup.MarkupException;

import spiralcraft.text.ParsePosition;

import spiralcraft.textgen.Element;

import spiralcraft.text.xml.Attribute;

import java.net.URI;

public class AssemblyElementFactory
  implements ElementFactory
{

  private static final ClassLogger log
    =ClassLogger.getInstance(AssemblyElementFactory.class);
  
  private final AssemblyClass assemblyClass;
  private final ParsePosition position;
  private URI namespaceUri;
  private String elementClassName;
  
  public AssemblyElementFactory
    (URI namespaceUri
    ,String elementName
    ,Attribute[] attributes
    ,ElementUnit[] properties
    ,ParsePosition position
    )
    throws MarkupException
  {
    elementClassName=elementName;
    this.position=position.clone();
    this.namespaceUri=namespaceUri;
    
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
      for (ElementUnit unit: properties)
      {
        PropertySpecifier property
          =new PropertySpecifier
          (assemblyClass
          ,unit.getPropertyName()
          );
        StringBuilder buf=new StringBuilder();
        for (TglUnit child : unit.getChildren())
        { 
          if (child instanceof ElementUnit)
          { 
            Expression<?> expression=((ElementUnit) child).getExpression();
            if (expression!=null)
            { property.setExpression(expression.getText());
            }
            else
            {
              throw new MarkupException
                ("Unsupported use of a child Element in a property Element"
                ,child.getPosition()
                );
            }
            // Translate the contents into a property def.
          }
          else if (child instanceof ContentUnit)
          {
            buf.append
              (((ContentUnit) child)
              .getContent().toString());
            
          }
        }
        if (buf.length()>0)
        { property.addCharacters(buf.toString().toCharArray());
        }
        assemblyClass.addPropertySpecifier(property);
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
           " Assembly or a Class"
          ,position
          );
      }
      else
      { throw new MarkupException(position,x);
      }
    }
    
  }
  
  
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
      return element;
    }
    catch (BuildException x)
    { 
      throw new MarkupException
        ("Error instantiating Element: "+x.toString(),position,x);
    }
    

  }
}
