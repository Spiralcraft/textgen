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
package spiralcraft.textgen.test;


import spiralcraft.textgen.Generator;
import spiralcraft.textgen.Element;

import spiralcraft.data.sax.DataReader;

import spiralcraft.data.Tuple;
import spiralcraft.data.Aggregate;

import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;

import spiralcraft.data.lang.TupleFocus;
import spiralcraft.data.lang.CursorBinding;

import spiralcraft.data.spi.ListCursor;

import spiralcraft.data.transport.Cursor;

import spiralcraft.lang.DefaultFocus;

import java.io.OutputStreamWriter;

import java.io.Writer;

import java.net.URI;


public class TupleTest
{
  public static void main(String[] args)
    throws Exception
  {
    singleTuple();
    listCursor();
    
  }
  
  public static void singleTuple()
    throws Exception
  {
    URI dataURI
      =GeneratorTest.class.getResource("model.data.xml").toURI();
  
    Tuple tuple
      =(Tuple) new DataReader().readFromURI(dataURI,null);

    URI uri=URI.create("java:/spiralcraft/textgen/test/tupleTest.tgl");



    TupleFocus<Tuple> focus=new TupleFocus<Tuple>(tuple.getType().getScheme());
    focus.setTuple(tuple);

    Generator generator=new Generator(uri);    
    Element element=generator.bind(focus);

    Writer writer=new OutputStreamWriter(System.out);
    element.write(writer);
    writer.flush();
    
  }
    
  @SuppressWarnings("unchecked") // Cast Object to Aggregate
  public static void listCursor()
    throws Exception
  {
    URI dataURI=URI.create
      ("java:/spiralcraft/data/test/example/Customer.data.xml");
    
    URI typeURI=URI.create
      ("java:/spiralcraft/data/test/example/Customer.list");
    
    Type type=TypeResolver.getTypeResolver().resolve(typeURI);

    Aggregate<Tuple> list
      =(Aggregate<Tuple>) new DataReader().readFromURI(dataURI,type);

    URI uri=URI.create("java:/spiralcraft/textgen/test/cursorTest.tgl");


    Cursor<Tuple> listCursor=new ListCursor<Tuple>(list);
    
    DefaultFocus<Tuple> focus
      =new DefaultFocus<Tuple>(new CursorBinding<Tuple>(listCursor));


    Generator generator=new Generator(uri);    
    Element element=generator.bind(focus);

    Writer writer=new OutputStreamWriter(System.out);
    element.write(writer);
    writer.flush();

  }
}