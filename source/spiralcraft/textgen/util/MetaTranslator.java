package spiralcraft.textgen.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStreamWriter;

import spiralcraft.vfs.Resource;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.TranslationException;
import spiralcraft.vfs.Translator;
import spiralcraft.vfs.AbstractResource;

import spiralcraft.textgen.Element;
import spiralcraft.textgen.Generator;
import spiralcraft.textgen.GenerationContext;

import spiralcraft.text.ParseException;
import spiralcraft.text.markup.MarkupException;

import spiralcraft.lang.BeanFocus;
import spiralcraft.lang.BindException;

import java.net.URI;



/**
 * <P>A Translator which describes a Resource. The Template is expressed 
 *   in terms of the Resource object, and can provide a formatted 
 *   description of the properties of the resource.
 *
 */
public class MetaTranslator
    implements Translator
{
  private URI templateURI;
  private Generator generator;
  private Resource templateResource;
  private long templateLastUpdated;

  public void setTemplateURI(URI templateURI)
    throws IOException,ParseException
  { 
    this.templateURI=templateURI;
    templateResource=Resolver.getInstance().resolve(templateURI);
    loadTemplate();
  }
  
  private synchronized void checkTemplateUpdate()
    throws IOException,ParseException
  {
    if (templateResource.getLastModified()>templateLastUpdated)
    { loadTemplate();
    }
  }
  
  private void loadTemplate()
    throws IOException,ParseException
  {
    long time=templateResource.getLastModified();
    
    generator=new Generator(templateURI);
    templateLastUpdated=time;

  }
  
  
  public Resource translate(Resource resource,URI translatedURI)
    throws IOException, TranslationException
  { 
    try
    { return new MetaResource(resource,translatedURI);
    }
    catch (BindException x)
    { 
      throw new IllegalArgumentException
        ("Error binding template "+generator.getSourceURI()
        +" to "+resource.toString()
        +": "+x
        );
    }
    catch (ParseException x)
    { 
      throw new IllegalArgumentException
        ("Error parsing template "+generator.getSourceURI()
        +" to "+resource.toString()
        +": "+x
        );
    }

  }
  
  public class MetaResource
    extends AbstractResource
  {
   
    private BeanFocus<MetaResource> focus;
    private Element element;
    private Resource resource;
    private long templateLastModified;
    
    public MetaResource(Resource resource,URI translatedURI)
      throws IOException,BindException,ParseException
    { 
      super(translatedURI);
      System.err.println("MetaResource: "+translatedURI);
      System.err.println("MetaResource: "+getURI());
      this.resource=resource;
      
      focus = new BeanFocus<MetaResource>(MetaResource.class,this);

      
      bind();
    }
    
    public Resource getResource()
    { return resource;
    }
    
    private void bind()
      throws BindException,MarkupException
    { element=generator.bind(focus);
    }
    
    private synchronized void checkLastModified()
      throws IOException
    { 
      try
      {
        checkTemplateUpdate();
        long time=templateLastUpdated;
        if (templateLastModified<time)
        { bind();
        }
        templateLastModified=time;
      }
      catch (BindException x)
      { 
        throw new IOException
          ("Error in translation template "+templateResource.toString()
          +": "+x
          ,x
          );
      }
      catch (ParseException x)
      { 
        throw new IOException
          ("Error parsing template "+generator.getSourceURI()
          +" to "+resource.toString()
          +": "+x
          );
      }
   
    }
    
   
    public long getLastModified()
      throws IOException
    { 
      checkLastModified();
      return Math.max
        (resource.getLastModified(),templateLastModified);
    }
    
    public InputStream getInputStream()
      throws IOException
    {
      checkLastModified();
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      OutputStreamWriter writer=new OutputStreamWriter(out);
      GenerationContext context=generator.createGenerationContext(writer);
      element.write(context);
      writer.flush();
      out.close();
      
      return new ByteArrayInputStream(out.toByteArray());
    }
    
  }

}
