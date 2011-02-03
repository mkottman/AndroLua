/*
 * $Id: JavaFunction.java,v 1.6 2006/12/22 14:06:40 thiago Exp $
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

/**
 * JavaFunction is a class that can be used to implement a Lua function in Java.
 * JavaFunction is an abstract class, so in order to use it you must extend this 
 * class and implement the <code>execute</code> method. This <code>execute</code> 
 * method is the method that will be called when you call the function from Lua.
 * To register the JavaFunction in Lua use the method <code>register(String name)</code>.
 */
public abstract class JavaFunction
{
	
	/**
	 * This is the state in which this function will exist.
	 */
	protected LuaState L;
	
	/**
	 * This method is called from Lua. Any parameters can be taken with
	 * <code>getParam</code>. A reference to the JavaFunctionWrapper itself is
	 * always the first parameter received. Values passed back as results
	 * of the function must be pushed onto the stack.
	 * @return The number of values pushed onto the stack.
	 */
	public abstract int execute() throws LuaException;
	
	/**
	 * Constructor that receives a LuaState.
	 * @param L LuaState object associated with this JavaFunction object
	 */
	public JavaFunction(LuaState L)
	{
		this.L = L;
	}

	/**
	 * Returns a parameter received from Lua. Parameters are numbered from 1.
	 * A reference to the JavaFunction itself is always the first parameter
	 * received (the same as <code>this</code>).
	 * @param idx Index of the parameter.
	 * @return Reference to parameter.
	 * @see LuaObject
	 */
	public LuaObject getParam(int idx)
	{
		return L.getLuaObject(idx);
	}

	/**
	 * Register a JavaFunction with a given name. This method registers in a
	 * global variable the JavaFunction specified.
	 * @param name name of the function.
	 */
	public void register(String name) throws LuaException
	{
	  synchronized (L)
	  {
			L.pushJavaFunction(this);
			L.setGlobal(name);
	  }
	}
}
