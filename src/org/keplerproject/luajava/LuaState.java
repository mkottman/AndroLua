/*
 * $Id: LuaState.java,v 1.9 2006/12/22 14:06:40 thiago Exp $
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
 * LuaState if the main class of LuaJava for the Java developer.
 * LuaState is a mapping of most of Lua's C API functions.
 * LuaState also provides many other functions that will be used to manipulate 
 * objects between Lua and Java.
 * @author Thiago Ponte
 */
public class LuaState
{
  private final static String LUAJAVA_LIB = "luajava";

  final public static Integer LUA_GLOBALSINDEX  = new Integer(-10002);
  final public static Integer LUA_REGISTRYINDEX = new Integer(-10000);

  final public static Integer LUA_TNONE     = new Integer(-1);
  final public static Integer LUA_TNIL      = new Integer(0);
  final public static Integer LUA_TBOOLEAN  = new Integer(1);
  final public static Integer LUA_TLIGHTUSERDATA = new Integer(2);
  final public static Integer LUA_TNUMBER   = new Integer(3);
  final public static Integer LUA_TSTRING   = new Integer(4);
  final public static Integer LUA_TTABLE    = new Integer(5);
  final public static Integer LUA_TFUNCTION = new Integer(6);
  final public static Integer LUA_TUSERDATA = new Integer(7);
  final public static Integer LUA_TTHREAD   = new Integer(8);

  /**
   * Specifies that an unspecified (multiple) number of return arguments
   * will be returned by a call.
   */
  final public static Integer LUA_MULTRET   = new Integer(-1);
  
  /*
   * error codes for `lua_load' and `lua_pcall'
   */
  /**
   * a runtime error.
   */
  final public static Integer LUA_ERRRUN    = new Integer(1);
  
  /**
   * 
   */
  final public static Integer LUA_YIELD     = new Integer(2);
  
  /**
   * syntax error during pre-compilation.
   */
  final public static Integer LUA_ERRSYNTAX = new Integer(3);
  
  /**
   * memory allocation error. For such errors, Lua does not call 
   * the error handler function.
   */
  final public static Integer LUA_ERRMEM    = new Integer(4);
  
  /**
   * error while running the error handler function.
   */
  final public static Integer LUA_ERRERR    = new Integer(5);

  /**
   * Opens the library containing the luajava API
   */
  static
  {
    System.loadLibrary(LUAJAVA_LIB);
  }

  private CPtr luaState;

  private int stateId;

  /**
   * Constructor to instance a new LuaState and initialize it with LuaJava's functions
   * @param stateId
   */
  protected LuaState(int stateId)
  {
    luaState = _open();
    luajava_open(luaState, stateId);
    this.stateId = stateId;
  }

  /**
   * Receives a existing state and initializes it
   * @param luaState
   */
  protected LuaState(CPtr luaState)
  {
    this.luaState = luaState;
    this.stateId = LuaStateFactory.insertLuaState(this);
    luajava_open(luaState, stateId);
  }

  /**
   * Closes state and removes the object from the LuaStateFactory
   */
  public synchronized void close()
  {
    LuaStateFactory.removeLuaState(stateId);
    _close(luaState);
    this.luaState = null;
  }
  
  /**
   * Returns <code>true</code> if state is closed.
   */
  public synchronized boolean isClosed()
  {
    return luaState == null;
  }

  /**
   * Return the long representing the LuaState pointer
   * @return long
   */
  public long getCPtrPeer()
  {
    return (luaState != null)? luaState.getPeer() : 0;
  }


  /********************* Lua Native Interface *************************/

  private synchronized native CPtr _open();
  private synchronized native void _close(CPtr ptr);
  private synchronized native CPtr _newthread(CPtr ptr);

  // Stack manipulation
  private synchronized native int  _getTop(CPtr ptr);
  private synchronized native void _setTop(CPtr ptr, int idx);
  private synchronized native void _pushValue(CPtr ptr, int idx);
  private synchronized native void _remove(CPtr ptr, int idx);
  private synchronized native void _insert(CPtr ptr, int idx);
  private synchronized native void _replace(CPtr ptr, int idx);
  private synchronized native int  _checkStack(CPtr ptr, int sz);
  
  private synchronized native void _xmove(CPtr from, CPtr to, int n);

  // Access functions
  private synchronized native int    _isNumber(CPtr ptr, int idx);
  private synchronized native int    _isString(CPtr ptr, int idx);
  private synchronized native int    _isCFunction(CPtr ptr, int idx);
  private synchronized native int    _isUserdata(CPtr ptr, int idx);
  private synchronized native int    _type(CPtr ptr, int idx);
  private synchronized native String _typeName(CPtr ptr, int tp);

  private synchronized native int _equal(CPtr ptr, int idx1, int idx2);
  private synchronized native int _rawequal(CPtr ptr, int idx1, int idx2);
  private synchronized native int _lessthan(CPtr ptr, int idx1, int idx2);

  private synchronized native double _toNumber(CPtr ptr, int idx);
  private synchronized native int    _toInteger(CPtr ptr, int idx);
  private synchronized native int    _toBoolean(CPtr ptr, int idx);
  private synchronized native String _toString(CPtr ptr, int idx);
  private synchronized native int    _objlen(CPtr ptr, int idx);
  private synchronized native CPtr   _toThread(CPtr ptr, int idx);

  // Push functions
  private synchronized native void _pushNil(CPtr ptr);
  private synchronized native void _pushNumber(CPtr ptr, double number);
  private synchronized native void _pushInteger(CPtr ptr, int integer);
  private synchronized native void _pushString(CPtr ptr, String str);
  private synchronized native void _pushString(CPtr ptr, byte[] bytes, int n);
  private synchronized native void _pushBoolean(CPtr ptr, int bool);

  // Get functions
  private synchronized native void _getTable(CPtr ptr, int idx);
  private synchronized native void _getField(CPtr ptr, int idx, String k);
  private synchronized native void _rawGet(CPtr ptr, int idx);
  private synchronized native void _rawGetI(CPtr ptr, int idx, int n);
  private synchronized native void _createTable(CPtr ptr, int narr, int nrec);
  private synchronized native int  _getMetaTable(CPtr ptr, int idx);
  private synchronized native void _getFEnv(CPtr ptr, int idx);

  // Set functions
  private synchronized native void _setTable(CPtr ptr, int idx);
  private synchronized native void _setField(CPtr ptr, int idx, String k);
  private synchronized native void _rawSet(CPtr ptr, int idx);
  private synchronized native void _rawSetI(CPtr ptr, int idx, int n);
  private synchronized native int  _setMetaTable(CPtr ptr, int idx);
  private synchronized native int  _setFEnv(CPtr ptr, int idx);

  private synchronized native void _call(CPtr ptr, int nArgs, int nResults);
  private synchronized native int  _pcall(CPtr ptr, int nArgs, int Results, int errFunc);

  // Coroutine Functions
  private synchronized native int _yield(CPtr ptr, int nResults);
  private synchronized native int _resume(CPtr ptr, int nargs);
  private synchronized native int _status(CPtr ptr);
  
  // Gargabe Collection Functions
  final public static Integer LUA_GCSTOP       = new Integer(0);
  final public static Integer LUA_GCRESTART    = new Integer(1);
  final public static Integer LUA_GCCOLLECT    = new Integer(2);
  final public static Integer LUA_GCCOUNT      = new Integer(3);
  final public static Integer LUA_GCCOUNTB     = new Integer(4);
  final public static Integer LUA_GCSTEP       = new Integer(5);
  final public static Integer LUA_GCSETPAUSE   = new Integer(6);
  final public static Integer LUA_GCSETSTEPMUL = new Integer(7);
  private synchronized native int  _gc(CPtr ptr, int what, int data);

  // Miscellaneous Functions
  private synchronized native int    _error(CPtr ptr);
  private synchronized native int    _next(CPtr ptr, int idx);
  private synchronized native void   _concat(CPtr ptr, int n);

  // Some macros
  private synchronized native void _pop(CPtr ptr, int n);
  private synchronized native void _newTable(CPtr ptr);
  private synchronized native int  _strlen(CPtr ptr, int idx);
  private synchronized native int  _isFunction(CPtr ptr, int idx);
  private synchronized native int  _isTable(CPtr ptr, int idx);
  private synchronized native int  _isNil(CPtr ptr, int idx);
  private synchronized native int  _isBoolean(CPtr ptr, int idx);
  private synchronized native int  _isThread(CPtr ptr, int idx);
  private synchronized native int  _isNone(CPtr ptr, int idx);
  private synchronized native int  _isNoneOrNil(CPtr ptr, int idx);
  
  private synchronized native void _setGlobal(CPtr ptr, String name);
  private synchronized native void _getGlobal(CPtr ptr, String name);
  
  private synchronized native int  _getGcCount(CPtr ptr);


  // LuaLibAux
  private synchronized native int _LdoFile(CPtr ptr, String fileName);
  private synchronized native int _LdoString(CPtr ptr, String string);
  //private synchronized native int _doBuffer(CPtr ptr, byte[] buff, long sz, String n);
  
  private synchronized native int    _LgetMetaField(CPtr ptr, int obj, String e);
  private synchronized native int    _LcallMeta(CPtr ptr, int obj, String e);
  private synchronized native int    _Ltyperror(CPtr ptr, int nArg, String tName);
  private synchronized native int    _LargError(CPtr ptr, int numArg, String extraMsg);
  private synchronized native String _LcheckString(CPtr ptr, int numArg);
  private synchronized native String _LoptString(CPtr ptr, int numArg, String def);
  private synchronized native double _LcheckNumber(CPtr ptr, int numArg);
  private synchronized native double _LoptNumber(CPtr ptr, int numArg, double def);
  
  private synchronized native int    _LcheckInteger(CPtr ptr, int numArg);
  private synchronized native int    _LoptInteger(CPtr ptr, int numArg, int def);
  
  private synchronized native void _LcheckStack(CPtr ptr, int sz, String msg);
  private synchronized native void _LcheckType(CPtr ptr, int nArg, int t);
  private synchronized native void _LcheckAny(CPtr ptr, int nArg);
  
  private synchronized native int  _LnewMetatable(CPtr ptr, String tName);
  private synchronized native void _LgetMetatable(CPtr ptr, String tName);
  
  private synchronized native void _Lwhere(CPtr ptr, int lvl);
  
  private synchronized native int  _Lref(CPtr ptr, int t);
  private synchronized native void _LunRef(CPtr ptr, int t, int ref);
  
  private synchronized native int  _LgetN(CPtr ptr, int t);
  private synchronized native void _LsetN(CPtr ptr, int t, int n);
  
  private synchronized native int _LloadFile(CPtr ptr, String fileName);
  private synchronized native int _LloadBuffer(CPtr ptr, byte[] buff, long sz, String name);
  private synchronized native int _LloadString(CPtr ptr, String s);

  private synchronized native String _Lgsub(CPtr ptr, String s, String p, String r);
  private synchronized native String _LfindTable(CPtr ptr, int idx, String fname, int szhint);
  
  
  private synchronized native void _openBase(CPtr ptr);
  private synchronized native void _openTable(CPtr ptr);
  private synchronized native void _openIo(CPtr ptr);
  private synchronized native void _openOs(CPtr ptr);
  private synchronized native void _openString(CPtr ptr);
  private synchronized native void _openMath(CPtr ptr);
  private synchronized native void _openDebug(CPtr ptr);
  private synchronized native void _openPackage(CPtr ptr);
  private synchronized native void _openLibs(CPtr ptr);

  // Java Interface -----------------------------------------------------

  public LuaState newThread()
  {
  	LuaState l = new LuaState(_newthread(luaState));
  	LuaStateFactory.insertLuaState(l);
    return l;
  }

  // STACK MANIPULATION

  public int getTop()
  {
    return _getTop(luaState);
  }

  public void setTop(int idx)
  {
    _setTop(luaState, idx);
  }

  public void pushValue(int idx)
  {
    _pushValue(luaState, idx);
  }

  public void remove(int idx)
  {
    _remove(luaState, idx);
  }

  public void insert(int idx)
  {
    _insert(luaState, idx);
  }

  public void replace(int idx)
  {
    _replace(luaState, idx);
  }

  public int checkStack(int sz)
  {
    return _checkStack(luaState, sz);
  }
  
  public void xmove(LuaState to, int n)
  {
    _xmove(luaState, to.luaState, n);
  }

  // ACCESS FUNCTION

  public boolean isNumber(int idx)
  {
    return (_isNumber(luaState, idx)!=0);
  }

  public boolean isString(int idx)
  {
    return (_isString(luaState, idx)!=0);
  }

  public boolean isFunction(int idx)
  {
    return (_isFunction(luaState, idx)!=0);
  }
  
  public boolean isCFunction(int idx)
  {
    return (_isCFunction(luaState, idx)!=0);
  }

  public boolean isUserdata(int idx)
  {
    return (_isUserdata(luaState, idx)!=0);
  }

  public boolean isTable(int idx)
  {
    return (_isTable(luaState, idx)!=0);
  }

  public boolean isBoolean(int idx)
  {
    return (_isBoolean(luaState, idx)!=0);
  }
  
  public boolean isNil(int idx)
  {
	  return (_isNil(luaState, idx)!=0);
  }
  
  public boolean isThread(int idx)
  {
	  return (_isThread(luaState, idx)!=0);
  }
  
  public boolean isNone(int idx)
  {
	  return (_isNone(luaState, idx)!=0);
  }
  
  public boolean isNoneOrNil(int idx)
  {
	  return (_isNoneOrNil(luaState, idx)!=0);
  }

  public int type(int idx)
  {
    return _type(luaState, idx);
  }

  public String typeName(int tp)
  {
  	return _typeName(luaState, tp);
  }

  public int equal(int idx1, int idx2)
  {
    return _equal(luaState, idx1, idx2);
  }

  public int rawequal(int idx1, int idx2)
  {
    return _rawequal(luaState, idx1, idx2);
  }

  public int lessthan(int idx1, int idx2)
  {
    return _lessthan(luaState, idx1, idx2);
  }

  public double toNumber(int idx)
  {
    return _toNumber(luaState, idx);
  }

  public int toInteger(int idx)
  {
	  return _toInteger(luaState, idx);
  }
  
  public boolean toBoolean(int idx)
  {
    return (_toBoolean(luaState, idx)!=0);
  }

  public String toString(int idx)
  {
    return _toString(luaState, idx);
  }
  
  public int strLen(int idx)
  {
    return _strlen(luaState, idx);
  }
  
  public int objLen(int idx)
  {
	  return _objlen(luaState, idx);
  }

  public LuaState toThread(int idx)
  {
    return new LuaState(_toThread(luaState, idx));
  }
  
  //PUSH FUNCTIONS
  
  public void pushNil()
  {
    _pushNil(luaState);
  }

  public void pushNumber(double db)
  {
    _pushNumber(luaState, db);
  }
  
  public void pushInteger(int integer)
  {
	  _pushInteger(luaState, integer);
  }

  public void pushString(String str)
  {
    if (str == null)
      _pushNil(luaState);
    else
      _pushString(luaState, str);
  }

  public void pushString(byte[] bytes)
  {
    if (bytes == null)
      _pushNil(luaState);
    else
      _pushString(luaState, bytes, bytes.length);
  }
  
  public void pushBoolean(boolean bool)
  {
    _pushBoolean(luaState, bool ? 1 : 0);
  }

  // GET FUNCTIONS

  public void getTable(int idx)
  {
    _getTable(luaState, idx);
  }
  
  public void getField(int idx, String k)
  {
	  _getField(luaState, idx, k);
  }

  public void rawGet(int idx)
  {
    _rawGet(luaState, idx);
  }

  public void rawGetI(int idx, int n)
  {
    _rawGetI(luaState, idx, n);
  }
  
  public void createTable(int narr, int nrec)
  {
	  _createTable(luaState, narr, nrec);
  }

  public void newTable()
  {
    _newTable(luaState);
  }

  // if returns 0, there is no metatable
  public int getMetaTable(int idx)
  {
    return _getMetaTable(luaState, idx);
  }

  public void getFEnv(int idx)
  {
    _getFEnv(luaState, idx);
  }

  // SET FUNCTIONS
  
  public void setTable(int idx)
  {
    _setTable(luaState, idx);
  }
  
  public void setField(int idx, String k)
  {
	  _setField(luaState, idx, k);
  }

  public void rawSet(int idx)
  {
    _rawSet(luaState, idx);
  }

  public void rawSetI(int idx, int n)
  {
    _rawSetI(luaState, idx, n);
  }

  // if returns 0, cannot set the metatable to the given object
  public int setMetaTable(int idx)
  {
    return _setMetaTable(luaState, idx);
  }

  // if object is not a function returns 0
  public int setFEnv(int idx)
  {
    return _setFEnv(luaState, idx);
  }

  public void call(int nArgs, int nResults)
  {
    _call(luaState, nArgs, nResults);
  }

  // returns 0 if ok of one of the error codes defined
  public int pcall(int nArgs, int nResults, int errFunc)
  {
    return _pcall(luaState, nArgs, nResults, errFunc);
  }

  public int yield(int nResults)
  {
  	return _yield(luaState, nResults);
  }

  public int resume(int nArgs)
  {
  	return _resume(luaState, nArgs);
  }
  
  public int status()
  {
	  return _status(luaState);
  }
  
  public int gc(int what, int data)
  {
    return _gc(luaState, what, data);
  }
  
  public int getGcCount()
  {
    return _getGcCount(luaState);
  }
  
  public int next(int idx)
  {
  	return _next(luaState, idx);
  }

  public int error()
  {
  	return _error(luaState);
  }

  public void concat(int n)
  {
  	_concat(luaState, n);
  }


  // FUNCTION FROM lauxlib
  // returns 0 if ok
  public int LdoFile(String fileName)
  {
    return _LdoFile(luaState, fileName);
  }

  // returns 0 if ok
  public int LdoString(String str)
  {
    return _LdoString(luaState, str);
  }
    
  public int LgetMetaField(int obj, String e)
  {
    return _LgetMetaField(luaState, obj, e);
  }
  
  public int LcallMeta(int obj, String e)
  {
    return _LcallMeta(luaState, obj, e);
  }
  
  public int Ltyperror(int nArg, String tName)
  {
    return _Ltyperror(luaState, nArg, tName);
  }
  
  public int LargError(int numArg, String extraMsg)
  {
    return _LargError(luaState, numArg, extraMsg);
  }
  
  public String LcheckString(int numArg)
  {
    return _LcheckString(luaState, numArg);
  }
  
  public String LoptString(int numArg, String def)
  {
    return _LoptString(luaState, numArg, def);
  }
  
  public double LcheckNumber(int numArg)
  {
    return _LcheckNumber(luaState, numArg);
  }
  
  public double LoptNumber(int numArg, double def)
  {
    return _LoptNumber(luaState, numArg, def);
  }
  
  public int LcheckInteger(int numArg)
  {
	  return _LcheckInteger(luaState, numArg);
  }
  
  public int LoptInteger(int numArg, int def)
  {
	  return _LoptInteger(luaState, numArg, def);
  }
  
  public void LcheckStack(int sz, String msg)
  {
    _LcheckStack(luaState, sz, msg);
  }
  
  public void LcheckType(int nArg, int t)
  {
    _LcheckType(luaState, nArg, t);
  }
  
  public void LcheckAny(int nArg)
  {
    _LcheckAny(luaState, nArg);
  }
  
  public int LnewMetatable(String tName)
  {
    return _LnewMetatable(luaState, tName);
  }
  
  public void LgetMetatable(String tName)
  {
    _LgetMetatable(luaState, tName);
  }
  
  public void Lwhere(int lvl)
  {
    _Lwhere(luaState, lvl);
  }
  
  public int Lref(int t)
  {
    return _Lref(luaState, t);
  }
  
  public void LunRef(int t, int ref)
  {
    _LunRef(luaState, t, ref);
  }
  
  public int LgetN(int t)
  {
    return _LgetN(luaState, t);
  }
  
  public void LsetN(int t, int n)
  {
    _LsetN(luaState, t, n);
  }
  
  public int LloadFile(String fileName)
  {
    return _LloadFile(luaState, fileName);
  }
  
  public int LloadString(String s)
  {
    return _LloadString(luaState, s);
  }
  
  public int LloadBuffer(byte[] buff, String name)
  {
    return _LloadBuffer(luaState, buff, buff.length, name);
  }
  
  public String Lgsub(String s, String p, String r)
  {
	  return _Lgsub(luaState, s, p, r);
  }
  
  public String LfindTable(int idx, String fname, int szhint)
  {
	  return _LfindTable(luaState, idx, fname, szhint);
  }
  
  //IMPLEMENTED C MACROS

  public void pop(int n)
  {
    //setTop(- (n) - 1);
	_pop(luaState, n);
  }

  public synchronized void getGlobal(String global)
  {
//    pushString(global);
//    getTable(LUA_GLOBALSINDEX.intValue());
    _getGlobal(luaState, global);
  }

  public synchronized void setGlobal(String name)
  {
    //pushString(name);
    //insert(-2);
    //setTable(LUA_GLOBALSINDEX.intValue());
	_setGlobal(luaState, name);
  }
  
  // Functions to open lua libraries
  public void openBase()
  {
    _openBase(luaState);
  }
  public void openTable()
  {
    _openTable(luaState);
  }
  public void openIo()
  {
    _openIo(luaState);
  }
  public void openOs()
  {
	  _openOs(luaState);
  }
  public void openString()
  {
    _openString(luaState);
  }
  public void openMath()
  {
    _openMath(luaState);
  }
  public void openDebug()
  {
    _openDebug(luaState);
  }
  public void openPackage()
  {
	  _openPackage(luaState);
  }
  public void openLibs()
  {
    _openLibs(luaState);
  }


  /********************** Luajava API Library **********************/

  /**
   * Initializes lua State to be used by luajava
   * @param cptr
   * @param stateId
   */
  private synchronized native void luajava_open(CPtr cptr, int stateId);
  /**
   * Gets a Object from a userdata
   * @param L
   * @param idx index of the lua stack
   * @return Object
   */
  private synchronized native Object _getObjectFromUserdata(CPtr L, int idx) throws LuaException;

  /**
   * Returns whether a userdata contains a Java Object
   * @param L
   * @param idx index of the lua stack
   * @return boolean
   */
  private synchronized native boolean _isObject(CPtr L, int idx);

  /**
   * Pushes a Java Object into the state stack
   * @param L
   * @param obj
   */
  private synchronized native void _pushJavaObject(CPtr L, Object obj);

  /**
   * Pushes a JavaFunction into the state stack
   * @param L
   * @param func
   */
  private synchronized native void _pushJavaFunction(CPtr L, JavaFunction func) throws LuaException;

  /**
   * Returns whether a userdata contains a Java Function
   * @param L
   * @param idx index of the lua stack
   * @return boolean
   */
  private synchronized native boolean _isJavaFunction(CPtr L, int idx);

  /**
   * Gets a Object from Lua
   * @param idx index of the lua stack
   * @return Object
   * @throws LuaException if the lua object does not represent a java object.
   */
  public Object getObjectFromUserdata(int idx) throws LuaException
  {
    return _getObjectFromUserdata(luaState, idx);
  }

  /**
   * Tells whether a lua index contains a java Object
   * @param idx index of the lua stack
   * @return boolean
   */
  public boolean isObject(int idx)
  {
    return _isObject(luaState, idx);
  }

  /**
   * Pushes a Java Object into the lua stack.<br>
   * This function does not check if the object is from a class that could
   * be represented by a lua type. Eg: java.lang.String could be a lua string.
   * @param obj Object to be pushed into lua
   */
  public void pushJavaObject(Object obj)
  {
    _pushJavaObject(luaState, obj);
  }

  /**
   * Pushes a JavaFunction into the state stack
   * @param func
   */
  public void pushJavaFunction(JavaFunction func) throws LuaException
  {
    _pushJavaFunction(luaState, func);
  }

  /**
   * Returns whether a userdata contains a Java Function
   * @param idx index of the lua stack
   * @return boolean
   */
  public boolean isJavaFunction(int idx)
  {
    return _isJavaFunction(luaState, idx);
  }

  /**
   * Pushes into the stack any object value.<br>
   * This function checks if the object could be pushed as a lua type, if not
   * pushes the java object.
   * @param obj
   */
  public void pushObjectValue(Object obj) throws LuaException
  {
    if (obj == null)
    {
      pushNil();
    }
    else if (obj instanceof Boolean)
    {
      Boolean bool = (Boolean) obj;
      pushBoolean(bool.booleanValue());
    }
    else if (obj instanceof Number)
    {
      pushNumber(((Number) obj).doubleValue());
    }
    else if (obj instanceof String)
    {
      pushString((String) obj);
    }
    else if (obj instanceof JavaFunction)
    {
      JavaFunction func = (JavaFunction) obj;
      pushJavaFunction(func);
    }
    else if (obj instanceof LuaObject)
    {
      LuaObject ref = (LuaObject) obj;
      ref.push();
    }
    else if (obj instanceof byte[])
    {
      pushString((byte[]) obj);
    }
    else
    {
      pushJavaObject(obj);
    }
  }

  /**
   * Function that returns a Java Object equivalent to the one in the given
   * position of the Lua Stack.
   * @param idx Index in the Lua Stack
   * @return Java object equivalent to the Lua one
   */
	public synchronized Object toJavaObject( int idx ) throws LuaException
	{
		Object obj = null;

		if (isBoolean(idx))
		{
			obj = new Boolean(toBoolean(idx));
		}
		else if (type(idx) == LuaState.LUA_TSTRING.intValue())
		{
			obj = toString(idx);
		}
		else if (isFunction(idx))
		{
			obj = getLuaObject(idx);
		}
		else if (isTable(idx))
		{
			obj = getLuaObject(idx);
		}
		else if (type(idx) == LuaState.LUA_TNUMBER.intValue())
		{
				obj = new Double(toNumber(idx));
		}
		else if (isUserdata(idx))
		{
			if (isObject(idx))
			{
				obj = getObjectFromUserdata(idx);
			}
			else
			{
				obj = getLuaObject(idx);
			}
		}
		else if (isNil(idx))
		{
			obj = null;
		}

		return obj;
	}

	/**
	 * Creates a reference to an object in the variable globalName
	 * @param globalName
	 * @return LuaObject
	 */
	public LuaObject getLuaObject(String globalName)
	{
		return new LuaObject(this, globalName);
	}

	/**
	 * Creates a reference to an object inside another object
     * @param parent The Lua Table or Userdata that contains the Field.
     * @param name The name that index the field
	 * @return LuaObject
	 * @throws LuaException if parent is not a table or userdata
	 */
	public LuaObject getLuaObject(LuaObject parent, String name)
		throws LuaException
	{
		if (parent.L.getCPtrPeer() != luaState.getPeer())
			throw new LuaException("Object must have the same LuaState as the parent!");

		return new LuaObject(parent, name);
	}

	/**
	 * This constructor creates a LuaObject from a table that is indexed by a number.
	 * @param parent The Lua Table or Userdata that contains the Field.
	 * @param name The name (number) that index the field
	 * @return LuaObject
	 * @throws LuaException When the parent object isn't a Table or Userdata
	 */
	public LuaObject getLuaObject(LuaObject parent, Number name)
		throws LuaException
	{
		if (parent.L.getCPtrPeer() != luaState.getPeer())
			throw new LuaException("Object must have the same LuaState as the parent!");

		return new LuaObject(parent, name);
	}
	
	/**
	 * This constructor creates a LuaObject from a table that is indexed by any LuaObject.
	 * @param parent The Lua Table or Userdata that contains the Field.
	 * @param name The name (LuaObject) that index the field
	 * @return LuaObject
	 * @throws LuaException When the parent object isn't a Table or Userdata
	 */
	public LuaObject getLuaObject(LuaObject parent, LuaObject name)
		throws LuaException
	{
	  if (parent.getLuaState().getCPtrPeer() != luaState.getPeer() ||
	      parent.getLuaState().getCPtrPeer() != name.getLuaState().getCPtrPeer())
	    throw new LuaException("Object must have the same LuaState as the parent!");

	  return new LuaObject(parent, name);
	}

	/**
	 * Creates a reference to an object in the <code>index</code> position
	 * of the stack
	 * @param index position on the stack
	 * @return LuaObject
	 */
	public LuaObject getLuaObject(int index)
	{
		return new LuaObject(this, index);
	}

	/**
	 * When you call a function in lua, it may return a number, and the
	 * number will be interpreted as a <code>Double</code>.<br>
	 * This function converts the number into a type specified by 
	 * <code>retType</code>
	 * @param db lua number to be converted
	 * @param retType type to convert to
	 * @return The converted number
	 */
	public static Number convertLuaNumber(Double db, Class retType)
	{
	  // checks if retType is a primitive type
    if (retType.isPrimitive())
    {
      if (retType == Integer.TYPE)
      {
        return new Integer(db.intValue());
      }
      else if (retType == Long.TYPE)
      {
        return new Long(db.longValue());
      }
      else if (retType == Float.TYPE)
      {
        return new Float(db.floatValue());
      }
      else if (retType == Double.TYPE)
      {
        return db;
      }
      else if (retType == Byte.TYPE)
      {
        return new Byte(db.byteValue());
      }
      else if (retType == Short.TYPE)
      {
        return new Short(db.shortValue());
      }
    }
    else if (retType.isAssignableFrom(Number.class))
    {
      // Checks all possibilities of number types
      if (retType.isAssignableFrom(Integer.class))
      {
        return new Integer(db.intValue());
      }
      else if (retType.isAssignableFrom(Long.class))
      {
        return new Long(db.longValue());
      }
      else if (retType.isAssignableFrom(Float.class))
      {
        return new Float(db.floatValue());
      }
      else if (retType.isAssignableFrom(Double.class))
      {
        return db;
      }
      else if (retType.isAssignableFrom(Byte.class))
      {
        return new Byte(db.byteValue());
      }
      else if (retType.isAssignableFrom(Short.class))
      {
        return new Short(db.shortValue());
      }
    }
    
    // if all checks fail, return null
    return null;	  
	}

	public String dumpStack() {
		int n = getTop();
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= n; i++) {
			int t = type(i);
			sb.append(i).append(": ").append(typeName(t));
			if (t == LUA_TNUMBER)
				sb.append(" = ").append(toNumber(i));
			else if (t == LUA_TSTRING)
				sb.append(" = '").append(toString(i)).append("'");
			sb.append("\n");
		}
		return sb.toString();
	}
}
