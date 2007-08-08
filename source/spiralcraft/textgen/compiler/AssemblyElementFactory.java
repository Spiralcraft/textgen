package spiralcraft.textgen.compiler;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.AssemblyClass;
import spiralcraft.builder.BuildException;
import spiralcraft.builder.PropertySpecifier;

import spiralcraft.text.markup.MarkupException;

import spiralcraft.text.ParsePosition;

import spiralcraft.textgen.Element;

import spiralcraft.xml.Attribute;

import java.net.URI;

public class AssemblyElementFactory
  implements ElementFactory
{

  private final AssemblyClass assemblyClass;
  private final ParsePosition position;
  
  public AssemblyElementFactory
    (URI namespaceUri
    ,String elementName
    ,Attribute[] attributes
    ,ParsePosition position
    )
    throws MarkupException
  {
    String elementClassName=elementName;
    this.position=position.clone();
    
    try
    {
      
      assemblyClass=new AssemblyClass
        (null
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
      assemblyClass.resolve();
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
    Assembly<?> parentAssembly=parentElement.getAssembly();
    try
    { 
      Assembly<?> assembly=assemblyClass.newInstance(parentAssembly);
      Element element=(Element) assembly.getSubject().get();    
      element.setAssembly(assembly);
      return element;
    }
    catch (BuildException x)
    { 
      throw new MarkupException
        ("Error instantiating Element: "+x.toString(),position,x);
    }
    

  }
}
