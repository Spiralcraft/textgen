//
// Copyright (c) 2010 Michael Toth
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
 * <p>Publishes the result of an Expression into the Focus chain for the
 *   lifetime of the associated State or until a trigger changes.
 * </p>
 * 
 * <p>The expression is recomputed every time the StateFrame changes, unless
 *   otherwise configured.
 * </p>
 * 
 * 
 * @author mike
 *
 * @param <T>
 */
public class Session<T>
  extends With<T>
{
  
  { defaultRecompute=false;
  }
}

