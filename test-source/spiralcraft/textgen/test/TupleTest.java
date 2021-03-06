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


import spiralcraft.textgen.EventContext;
import spiralcraft.textgen.RenderMessage;

import spiralcraft.textgen.compiler.TglCompiler;
import spiralcraft.textgen.compiler.DocletUnit;

import spiralcraft.app.Component;
import spiralcraft.data.sax.DataReader;

import spiralcraft.data.Tuple;
import spiralcraft.data.Aggregate;

import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;

import spiralcraft.data.lang.TupleFocus;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.exec.Executable;
import spiralcraft.exec.ExecutionException;

import spiralcraft.lang.SimpleFocus;

import spiralcraft.lang.spi.SimpleChannel;

import java.io.OutputStreamWriter;

import java.io.Writer;

import java.net.URI;


public class TupleTest
  implements Executable
{
  @Override
  public void execute(String ... args)
    throws ExecutionException
  {    
    try
    {
      singleTuple();
      listCursor();
    }
    catch (Exception x)
    { throw new ExecutionException("Error",x);
    }    
  }
  
  public static void singleTuple()
    throws Exception
  {
    URI dataURI
      =URI.create("class:/spiralcraft/textgen/test/model.data.xml");
  
    Tuple tuple
      =(Tuple) new DataReader().readFromURI(dataURI,null);

    URI uri=URI.create("class:/spiralcraft/textgen/test/tupleTest.tgl");



    TupleFocus<Tuple> focus
      =TupleFocus.<Tuple>create
        (null,tuple.getType().getScheme());
    
    focus.setTuple(tuple);

    DocletUnit unit=new TglCompiler<DocletUnit>().compile(uri);    
    Component element=unit.bind(focus,null);

    Writer writer=new OutputStreamWriter(System.out);
    EventContext context=new EventContext(writer,false,null);
    context.dispatch(RenderMessage.INSTANCE,element,null);
    writer.flush();
    
  }
    
  @SuppressWarnings({ "unchecked", "rawtypes" }) // Cast Object to Aggregate
  public static void listCursor()
    throws Exception
  {
    URI dataURI=URI.create
      ("class:/spiralcraft/data/test/example/Customer.data.xml");
    
    URI typeURI=URI.create
      ("class:/spiralcraft/data/test/example/Customer.list");
    
    Type type=TypeResolver.getTypeResolver().resolve(typeURI);

    Aggregate<Tuple> list
      =(Aggregate<Tuple>) new DataReader().readFromURI(dataURI,type);

    URI uri=URI.create("class:/spiralcraft/textgen/test/cursorTest.tgl");

    SimpleChannel binding
      =new SimpleChannel(DataReflector.getInstance(list.getType()),list,false);
    
    SimpleFocus<Aggregate> focus
      =new SimpleFocus<Aggregate>(binding);


    DocletUnit unit=new TglCompiler().compile(uri);    
    Component element=unit.bind(focus,null);

    Writer writer=new OutputStreamWriter(System.out);
    EventContext context=new EventContext(writer,false,null);
    context.dispatch(RenderMessage.INSTANCE,element,null);
    writer.flush();

  }
}