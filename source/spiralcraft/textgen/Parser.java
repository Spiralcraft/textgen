package spiralcraft.textgen;

import spiralcraft.text.KmpMatcher;

/**
 * Parser for text generation markup language.
 */
public class Parser
{
  private ContentHandler _contentHandler;
  private final KmpMatcher _beginTagMatcher
    =new KmpMatcher("<%");
  private final KmpMatcher _endTagMatcher
    =new KmpMatcher("%>");

  /**
   * Supply a ContentHandler for the parser to process
   *   the text and code fragments read from the input.
   */
  public void setContentHandler(ContentHandler val)
  { _contentHandler=val;
  }
  
  public void parse(CharSequence sequence)
    throws ParseException
  {
    boolean inText=true;
    _beginTagMatcher.reset();
    _endTagMatcher.reset();
    int mark=0;
    for (int i=0;i<sequence.length();i++)
    {
      if (inText)
      { 
        if (_beginTagMatcher.match(sequence.charAt(i)))
        { 
          _contentHandler.handleText(sequence.subSequence(mark,i-1));
          mark=i+1;
          inText=false;
        }
      }
      else
      {
        if (_endTagMatcher.match(sequence.charAt(i)))
        { 
          _contentHandler.handleCode(sequence.subSequence(mark,i-1));
          mark=i+1;
          inText=true;
        }
      }
    }
    if(!inText)
    { throw new ParseException("Unexpected end of input. Missing %>");
    }
    _contentHandler.handleText(sequence.subSequence(mark,sequence.length()));
  }
}
