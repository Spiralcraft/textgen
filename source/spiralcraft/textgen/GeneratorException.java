package spiralcraft.textgen;

public class GeneratorException
  extends Exception
{
  public GeneratorException(String message)
  { super(message);
  }
  
  public GeneratorException(String message,Throwable cause)
  { super(message,cause);
  }

  public GeneratorException(Throwable cause)
  { super(cause);
  }
}
