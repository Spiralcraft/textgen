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

import spiralcraft.vfs.spi.AbstractResource;

import spiralcraft.textgen.RenderMessage;
import spiralcraft.textgen.compiler.TglCompiler;
import spiralcraft.textgen.compiler.DocletUnit;
import spiralcraft.textgen.compiler.TglUnit;
import spiralcraft.textgen.EventContext;

import spiralcraft.text.ParseException;

import spiralcraft.app.Component;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.reflect.BeanFocus;
import spiralcraft.log.ClassLog;
import spiralcraft.lang.BindException;

import java.net.URI;



/**
 * A Translator which describes a Resource. The Template is expressed 
 *   in terms of the Resource object, and can provide a formatted 
 *   description of the properties of the resource.
 *
 */
public class MetaTranslator
    implements Translator
{
  private static final ClassLog log=ClassLog.getInstance(MetaTranslator.class);
  
  private URI templateURI;
  private TglUnit tglUnit;
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
    
    tglUnit=new TglCompiler<DocletUnit>().compile(templateURI);
    templateLastUpdated=time;

  }
  
  
  @Override
  public Resource translate(Resource resource,URI translatedURI,Resource existingResource)
    throws IOException, TranslationException
  { return translate(resource,translatedURI);
  }
  
  @Override
  public Resource translate(Resource resource,URI translatedURI)
    throws IOException, TranslationException
  { 
    try
    { return new MetaResource(resource,translatedURI);
    }
    catch (ContextualException x)
    { 
      throw new IllegalArgumentException
        ("Error in template "+templateURI
        +" for "+resource.toString()
        +": "+x
        );
    }
  }
  
  public class MetaResource
    extends AbstractResource
  {
   
    private BeanFocus<MetaResource> focus;
    private Component element;
    private Resource resource;
    private long templateLastModified;
    
    /**
     * 
     * @param resource
     * @param translatedURI
     * @throws BindException
     * @throws ParseException
     */
    public MetaResource(Resource resource,URI translatedURI)
      throws ContextualException
    { 
      super(translatedURI);
      log.fine("MetaResource: "+translatedURI);
      log.fine("MetaResource: "+getURI());
      this.resource=resource;
      
      focus = new BeanFocus<MetaResource>(MetaResource.class,this);

      
      bind();
    }
    
    public Resource getResource()
    { return resource;
    }
    
    @Override
    public void delete()
      throws IOException
    { throw new IOException("Rename not supported "+toString());
    }
    
    @Override
    public void renameTo(URI name)
      throws IOException
    { throw new IOException("Rename not supported "+toString());
    }
    
    private void bind()
      throws ContextualException
    { element=tglUnit.bind(focus,null);
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

      catch (ContextualException x)
      { 
        throw new IOException
          ("Error parsing template "+templateURI
          +" to "+resource.toString()
          +": "+x
          );
      }
   
    }
    
   
    @Override
    public long getLastModified()
      throws IOException
    { 
      checkLastModified();
      return Math.max
        (resource.getLastModified(),templateLastModified);
    }
    
    @Override
    public InputStream getInputStream()
      throws IOException
    {
      checkLastModified();
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      OutputStreamWriter writer=new OutputStreamWriter(out);
      EventContext context=new EventContext(writer,false,null);
      context.dispatch(RenderMessage.INSTANCE,element,null);
      writer.flush();
      out.close();
      
      return new ByteArrayInputStream(out.toByteArray());
    }
    
  }

}
