package spiralcraft.textgen;

public class MarkupException
  extends GeneratorException
{
  public MarkupException(String message)
  { super(message);
  }
  
  public MarkupException(String message,Throwable cause)
  { super(message,cause);
  }

  public MarkupException(Throwable cause)
  { super(cause);
  }
}
