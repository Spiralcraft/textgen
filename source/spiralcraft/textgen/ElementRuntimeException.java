package spiralcraft.textgen;

import java.util.ArrayList;

public class ElementRuntimeException
  extends RuntimeException
{
  

  private static final long serialVersionUID = 1L;
  private ArrayList<String> detail
    =new ArrayList<String>();

  public ElementRuntimeException(Element e,RuntimeException x)
  { 
    super("Uncaught exception: ",x);
    addDetail(e);
  }

  public void addDetail(Element x)
  { 
    StringBuffer detailStr=new StringBuffer();
    if (x.getAssembly()!=null)
    { detailStr.append(x.getAssembly().getAssemblyClass().getSourceURI()+" - ");
    }
    else
    { detailStr.append(x.getClass().getName()+" - ");
    }
    if (x.getCodePosition()!=null)
    { detailStr.append(x.getCodePosition().toString());
    }

    if (detailStr.length()>0)
    { detail.add("\r\n    "+detailStr);
    }
    
  }
  
  @Override
  public String toString()
  { return super.toString()+": "+detail.toString();
  }
}
