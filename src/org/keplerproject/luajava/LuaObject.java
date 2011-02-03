/*
 * $Id: LuaObject.java,v 1.6 2006/12/22 14:06:40 thiago Exp $
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
import java.lang.reflect.Proxy;
import java.util.StringTokenizer;

/**
 * This class represents a Lua object of any type. A LuaObject is constructed by a {@link LuaState} object using one of
 * the four methods:
 * <ul>
 * <li>{@link LuaState#getLuaObject(String globalName)}</li>
 * <li>{@link LuaState#getLuaObject(LuaObject parent, String name)}</li>
 * <li>{@link LuaState#getLuaObject(LuaObject parent, Number name)}</li>
 * <li>{@link LuaState#getLuaObject(LuaObject parent, LuaObject name)}</li>
 * <li>{@link LuaState#getLuaObject(int index)}</li>
 * </ul>
 * The LuaObject will represent only the object itself, not a variable or a stack index, so when you change a string,
 * remember that strings are immutable objects in Lua, and the LuaObject you have will represent the old one.
 * 
 * <h2>Proxies</h2>
 * 
 * LuaJava allows you to implement a class in Lua, like said before. If you want to create this proxy from Java, you
 * should have a LuaObject representing the table that has the functions that implement the interface. From this
 * LuaObject you can call the <code>createProxy(String implements)</code>. This method receives the string with the
 * name of the interfaces implemented by the object separated by comma.
 * 
 * @author Rizzato
 * @author Thiago Ponte
 */
public class LuaObject
{
	protected Integer ref;

	protected LuaState L;

	/**
	 * Creates a reference to an object in the variable globalName
	 * 
	 * @param L
	 * @param globalName
	 */
	protected LuaObject(LuaState L, String globalName)
	{
		synchronized (L)
		{
			this.L = L;
			L.getGlobal(globalName);
			registerValue(-1);
			L.pop(1);
		}
	}

	/**
	 * Creates a reference to an object inside another object
	 * 
	 * @param parent
	 *            The Lua Table or Userdata that contains the Field.
	 * @param name
	 *            The name that index the field
	 */
	protected LuaObject(LuaObject parent, String name) throws LuaException
	{
		synchronized (parent.getLuaState())
		{
			this.L = parent.getLuaState();

			if (!parent.isTable() && !parent.isUserdata())
			{
				throw new LuaException("Object parent should be a table or userdata .");
			}

			parent.push();
			L.pushString(name);
			L.getTable(-2);
			L.remove(-2);
			registerValue(-1);
			L.pop(1);
		}
	}

	/**
	 * This constructor creates a LuaObject from a table that is indexed by a number.
	 * 
	 * @param parent
	 *            The Lua Table or Userdata that contains the Field.
	 * @param name
	 *            The name (number) that index the field
	 * @throws LuaException
	 *             When the parent object isn't a Table or Userdata
	 */
	protected LuaObject(LuaObject parent, Number name) throws LuaException
	{
		synchronized (parent.getLuaState())
		{
			this.L = parent.getLuaState();
			if (!parent.isTable() && !parent.isUserdata())
				throw new LuaException("Object parent should be a table or userdata .");

			parent.push();
			L.pushNumber(name.doubleValue());
			L.getTable(-2);
			L.remove(-2);
			registerValue(-1);
			L.pop(1);
		}
	}

	/**
	 * This constructor creates a LuaObject from a table that is indexed by a LuaObject.
	 * 
	 * @param parent
	 *            The Lua Table or Userdata that contains the Field.
	 * @param name
	 *            The name (LuaObject) that index the field
	 * @throws LuaException
	 *             When the parent object isn't a Table or Userdata
	 */
	protected LuaObject(LuaObject parent, LuaObject name) throws LuaException
	{
		if (parent.getLuaState() != name.getLuaState())
			throw new LuaException("LuaStates must be the same!");
		synchronized (parent.getLuaState())
		{
			if (!parent.isTable() && !parent.isUserdata())
				throw new LuaException("Object parent should be a table or userdata .");

			this.L = parent.getLuaState();

			parent.push();
			name.push();
			L.getTable(-2);
			L.remove(-2);
			registerValue(-1);
			L.pop(1);
		}
	}

	/**
	 * Creates a reference to an object in the given index of the stack
	 * 
	 * @param L
	 * @param index
	 *            of the object on the lua stack
	 */
	protected LuaObject(LuaState L, int index)
	{
		synchronized (L)
		{
			this.L = L;

			registerValue(index);
		}
	}

	/**
	 * Gets the Object's State
	 */
	public LuaState getLuaState()
	{
		return L;
	}

	/**
	 * Creates the reference to the object in the registry table
	 * 
	 * @param index
	 *            of the object on the lua stack
	 */
	private void registerValue(int index)
	{
		synchronized (L)
		{
			L.pushValue(index);
			int key = L.Lref(LuaState.LUA_REGISTRYINDEX.intValue());
			ref = new Integer(key);
		}
	}

	protected void finalize()
	{
		try
		{
			synchronized (L)
			{
				if (L.getCPtrPeer() != 0)
					L.LunRef(LuaState.LUA_REGISTRYINDEX.intValue(), ref.intValue());
			}
		}
		catch (Exception e)
		{
			System.err.println("Unable to release object " + ref);
		}
	}

	/**
	 * Pushes the object represented by <code>this<code> into L's stack
	 */
	public void push()
	{
		L.rawGetI(LuaState.LUA_REGISTRYINDEX.intValue(), ref.intValue());
	}

	public boolean isNil()
	{
		synchronized (L)
		{
			push();
			boolean bool = L.isNil(-1);
			L.pop(1);
			return bool;
		}
	}

	public boolean isBoolean()
	{
		synchronized (L)
		{
			push();
			boolean bool = L.isBoolean(-1);
			L.pop(1);
			return bool;
		}
	}

	public boolean isNumber()
	{
		synchronized (L)
		{
			push();
			boolean bool = L.isNumber(-1);
			L.pop(1);
			return bool;
		}
	}

	public boolean isString()
	{
		synchronized (L)
		{
			push();
			boolean bool = L.isString(-1);
			L.pop(1);
			return bool;
		}
	}

	public boolean isFunction()
	{
		synchronized (L)
		{
			push();
			boolean bool = L.isFunction(-1);
			L.pop(1);
			return bool;
		}
	}

	public boolean isJavaObject()
	{
		synchronized (L)
		{
			push();
			boolean bool = L.isObject(-1);
			L.pop(1);
			return bool;
		}
	}

	public boolean isJavaFunction()
	{
		synchronized (L)
		{
			push();
			boolean bool = L.isJavaFunction(-1);
			L.pop(1);
			return bool;
		}
	}

	public boolean isTable()
	{
		synchronized (L)
		{
			push();
			boolean bool = L.isTable(-1);
			L.pop(1);
			return bool;
		}
	}

	public boolean isUserdata()
	{
		synchronized (L)
		{
			push();
			boolean bool = L.isUserdata(-1);
			L.pop(1);
			return bool;
		}
	}

	public int type()
	{
		synchronized (L)
		{
			push();
			int type = L.type(-1);
			L.pop(1);
			return type;
		}
	}

	public boolean getBoolean()
	{
		synchronized (L)
		{
			push();
			boolean bool = L.toBoolean(-1);
			L.pop(1);
			return bool;
		}
	}

	public double getNumber()
	{
		synchronized (L)
		{
			push();
			double db = L.toNumber(-1);
			L.pop(1);
			return db;
		}
	}

	public String getString()
	{
		synchronized (L)
		{
			push();
			String str = L.toString(-1);
			L.pop(1);
			return str;
		}
	}

	public Object getObject() throws LuaException
	{
		synchronized (L)
		{
			push();
			Object obj = L.getObjectFromUserdata(-1);
			L.pop(1);
			return obj;
		}
	}

	/**
	 * If <code>this<code> is a table or userdata tries to set
	 * a field value.
	 */
	public LuaObject getField(String field) throws LuaException
	{
		return L.getLuaObject(this, field);
	}

	/**
	 * Calls the object represented by <code>this</code> using Lua function pcall.
	 * 
	 * @param args -
	 *            Call arguments
	 * @param nres -
	 *            Number of objects returned
	 * @return Object[] - Returned Objects
	 * @throws LuaException
	 */
	public Object[] call(Object[] args, int nres) throws LuaException
	{
		synchronized (L)
		{
			if (!isFunction() && !isTable() && !isUserdata())
				throw new LuaException("Invalid object. Not a function, table or userdata .");

			int top = L.getTop();
			push();
			int nargs;
			if (args != null)
			{
				nargs = args.length;
				for (int i = 0; i < nargs; i++)
				{
					Object obj = args[i];
					L.pushObjectValue(obj);
				}
			}
			else
				nargs = 0;

			int err = L.pcall(nargs, nres, 0);

			if (err != 0)
			{
				String str;
				if (L.isString(-1))
				{
					str = L.toString(-1);
					L.pop(1);
				}
				else
					str = "";

				if (err == LuaState.LUA_ERRRUN.intValue())
				{
					str = "Runtime error. " + str;
				}
				else if (err == LuaState.LUA_ERRMEM.intValue())
				{
					str = "Memory allocation error. " + str;
				}
				else if (err == LuaState.LUA_ERRERR.intValue())
				{
					str = "Error while running the error handler function. " + str;
				}
				else
				{
					str = "Lua Error code " + err + ". " + str;
				}

				throw new LuaException(str);
			}

			if (nres == LuaState.LUA_MULTRET.intValue())
				nres = L.getTop() - top;
			if (L.getTop() - top < nres)
			{
				throw new LuaException("Invalid Number of Results .");
			}

			Object[] res = new Object[nres];

			for (int i = nres; i > 0; i--)
			{
				res[i - 1] = L.toJavaObject(-1);
				L.pop(1);
			}
			return res;
		}
	}

	/**
	 * Calls the object represented by <code>this</code> using Lua function pcall. Returns 1 object
	 * 
	 * @param args -
	 *            Call arguments
	 * @return Object - Returned Object
	 * @throws LuaException
	 */
	public Object call(Object[] args) throws LuaException
	{
		return call(args, 1)[0];
	}

	public String toString()
	{
		synchronized (L)
		{
			try
			{
				if (isNil())
					return "nil";
				else if (isBoolean())
					return String.valueOf(getBoolean());
				else if (isNumber())
					return String.valueOf(getNumber());
				else if (isString())
					return getString();
				else if (isFunction())
					return "Lua Function";
				else if (isJavaObject())
					return getObject().toString();
				else if (isUserdata())
					return "Userdata";
				else if (isTable())
					return "Lua Table";
				else if (isJavaFunction())
					return "Java Function";
				else
					return null;
			}
			catch (LuaException e)
			{
				return null;
			}
		}
	}

	/**
	 * Function that creates a java proxy to the object represented by <code>this</code>
	 * 
	 * @param implem
	 *            Interfaces that are implemented, separated by <code>,</code>
	 */
	public Object createProxy(String implem) throws ClassNotFoundException, LuaException
	{
		synchronized (L)
		{
			if (!isTable())
				throw new LuaException("Invalid Object. Must be Table.");

			StringTokenizer st = new StringTokenizer(implem, ",");
			Class[] interfaces = new Class[st.countTokens()];
			for (int i = 0; st.hasMoreTokens(); i++)
				interfaces[i] = Class.forName(st.nextToken());

			InvocationHandler handler = new LuaInvocationHandler(this);

			return Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaces, handler);
		}
	}
}
