package spiralcraft.textgen;

public class ParseException
  extends GeneratorException
{
  public ParseException(String message)
  { super(message);
  }
  
  public ParseException(String message,Throwable cause)
  { super(message,cause);
  }

  public ParseException(Throwable cause)
  { super(cause);
  }
}
