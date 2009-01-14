//
//Copyright (c) 2009,2009 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.textgen;

import java.io.IOException;

/**
 * <p>Renders content to the EventContext on behalf on an Element
 * </p>
 * 
 * @author mike
 */
public interface Renderer
{
  public void render(EventContext context,boolean postOrder)
    throws IOException;
}
