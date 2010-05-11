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

import spiralcraft.textgen.ExpressionFocusElement;


/**
 * <p>Creates a new Focus for the result of an expression evaluation and 
 *   the computed value for repeated access by this element's children
 *   during a render or message cycle.
 * </p>
 * 
 * <p>By default, this element will invalidate before prepare and after 
 *   rendering. If data is valid between rendering and a future prepare
 *   stage, set invalidateAfterRender=false
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class With<T>
  extends ExpressionFocusElement<T>
{

  
}
