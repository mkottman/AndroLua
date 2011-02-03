/*
 * $Id: Console.java,v 1.7 2006/12/22 14:06:40 thiago Exp $
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

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Simple LuaJava console.
 * This is also an example on how to use the Java side of LuaJava and how to startup
 * a LuaJava application.
 * 
 * @author Thiago Ponte
 */
public class Console
{

   /**
    * Creates a console for user interaction.
    * 
    * @param args	names of the lua files to be executed
    */
   public static void main(String[] args)
   {
      try
      {
         LuaState L = LuaStateFactory.newLuaState();
         L.openLibs();

         if (args.length > 0)
         {
            for (int i = 0; i < args.length; i++)
            {
               int res = L.LloadFile(args[i]);
               if (res == 0)
               {
                  res = L.pcall(0, 0, 0);
               }
               if (res != 0)
               {
                  throw new LuaException("Error on file: " + args[i] + ". " + L.toString(-1));
               }
            }

            return;
         }

         System.out.println("API Lua Java - console mode.");

         BufferedReader inp = new BufferedReader(new InputStreamReader(System.in));

         String line;

         System.out.print("> ");
         while ((line = inp.readLine()) != null && !line.equals("exit"))
         {
            int ret = L.LloadBuffer(line.getBytes(), "from console");
            if (ret == 0)
            {
               ret = L.pcall(0, 0, 0);
            }
            if (ret != 0)
            {
               System.err.println("Error on line: " + line);
               System.err.println(L.toString(-1));
            }
            System.out.print("> ");
         }

         L.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

   }
}