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

import java.util.LinkedList;

import spiralcraft.textgen.MarkupException;
import spiralcraft.textgen.Element;

import spiralcraft.builder.Assembly;
import spiralcraft.builder.BuildException;

import spiralcraft.lang.BindException;

/**
 * A Unit of text generation which represents a
 *   node in the tree structure of a TGL block.
 */
public interface TglUnit
{
  
  /**
   * Create a tree of Elements bound into an application context (the Assembly)
   *   which implements the functional behavior specified by the TGL 
   *   document.
   */
  public Element bind(Assembly parent,Element parentElement)
    throws BuildException,BindException;

  
}
