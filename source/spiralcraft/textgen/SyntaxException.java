package spiralcraft.textgen;

public class SyntaxException
  extends GeneratorException
{
  public SyntaxException(String message)
  { super(message);
  }
  
  public SyntaxException(String message,Throwable cause)
  { super(message,cause);
  }

  public SyntaxException(Throwable cause)
  { super(cause);
  }
}
