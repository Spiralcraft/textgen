//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.textgen.compiler;

/**
 * <p>Abstract base class for units involved in processing the structure and
 *   defining the metadata of the 
 * </p>
 * 
 * @author mike
 *
 */
public abstract class ProcessingUnit
  extends TglUnit
{

  protected boolean open=true;
  
  public ProcessingUnit(TglUnit parent)
  { super(parent);
  }

  @Override
  public boolean isOpen()
  { return open;
  }
  
  @Override
  public void close()
  { open=false;
  }
  
  
}
