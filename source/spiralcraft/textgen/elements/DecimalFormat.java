//
// Copyright (c) 2008,2009 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.textgen.elements;


/**
 * <p>Formats a decimal number
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class DecimalFormat
  extends Format<java.text.DecimalFormat>
{
  
  private String formatString;
  
  public void setFormat(String formatString)
  { this.formatString=formatString;
  }
  
  @Override
  protected java.text.DecimalFormat createFormat()
  { return new java.text.DecimalFormat(formatString);
  }

}
