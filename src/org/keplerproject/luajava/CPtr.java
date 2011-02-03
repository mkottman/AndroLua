/*
 * $Id: CPtr.java,v 1.4 2006/12/22 14:06:40 thiago Exp $
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
 * An abstraction for a C pointer data type.  A CPtr instance represents, on
 * the Java side, a C pointer.  The C pointer could be any <em>type</em> of C
 * pointer. 
 */
public class CPtr
{
    
    /**
     * Compares this <code>CPtr</code> to the specified object.
     *
     * @param other a <code>CPtr</code>
     * @return      true if the class of this <code>CPtr</code> object and the
     *		    class of <code>other</code> are exactly equal, and the C
     *		    pointers being pointed to by these objects are also
     *		    equal. Returns false otherwise.
     */
	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		if (other == this)
	    return true;
		if (CPtr.class != other.getClass())
	    return false;
		return peer == ((CPtr)other).peer;
   }


    /* Pointer value of the real C pointer. Use long to be 64-bit safe. */
    private long peer;
    
    /**
     * Gets the value of the C pointer abstraction
     * @return long
     */
    protected long getPeer()
    {
    	return peer;
    }

    /* No-args constructor. */
    CPtr() {}
 
}
