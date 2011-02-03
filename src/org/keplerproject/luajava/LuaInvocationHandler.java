/*
 * $Id: LuaInvocationHandler.java,v 1.4 2006/12/22 14:06:40 thiago Exp $
 * Copyright (C) 2003-2007 Kepler Project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.keplerproject.luajava;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Class that implements the InvocationHandler interface.
 * This class is used in the LuaJava's proxy system.
 * When a proxy object is accessed, the method invoked is
 * called from Lua
 * @author Rizzato
 * @author Thiago Ponte
 */
public class LuaInvocationHandler implements InvocationHandler
{
	private LuaObject obj;

	
	public LuaInvocationHandler(LuaObject obj)
	{
		this.obj = obj;
	}
	
	/**
	 * Function called when a proxy object function is invoked.
	 */
  public Object invoke(Object proxy, Method method, Object[] args) throws LuaException
  {
    synchronized(obj.L)
    {
	  	String methodName = method.getName();
	  	LuaObject func    = obj.getField(methodName);
	  	
	  	if ( func.isNil() )
	  	{
	  		return null;
	  	}
	  	
	  	Class retType = method.getReturnType();
	  	Object ret;

	  	// Checks if returned type is void. if it is returns null.
	  	if ( retType.equals( Void.class ) || retType.equals( void.class ) )
	  	{
	  		func.call( args , 0 );
	  		ret = null;
	  	}
	  	else
	  	{
	  		ret = func.call(args, 1)[0];
	  		if( ret != null && ret instanceof Double )
	  		{
	  		  ret = LuaState.convertLuaNumber((Double) ret, retType);
	  		}
	  	}
	  	
	  	return ret;
	  }
  }
}
