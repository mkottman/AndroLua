AndroLua
========

AndroLua is the [Lua](http://www.lua.org/) interpreter ported to the [Android](http://www.android.com/) platform. Others have ported Lua to Android, but this project is special:

* it includes [LuaJava](http://www.keplerproject.org/luajava/), so you can access (almost) everything the [Android API](http://developer.android.com/reference/classes.html) provides
* because writing code on the soft keyboard can be hard, you can connect to it using TCP an upload code from your computer

I created it because I wanted to learn how to use the [Android NDK](http://developer.android.com/sdk/ndk/index.html) and explore the Android API without having to go through the fuss of creating a project, writing boilerplate code, compiling and uploading the APK just to test a few lines of code.

Depending on the interest, it may become something more...

Requirements
------------

* [Android SDK](http://developer.android.com/sdk/index.html)
* [Android NDK](http://developer.android.com/sdk/ndk/index.html)
* (optionally) Eclipse with the [ADT](http://developer.android.com/sdk/eclipse-adt.html) plugin

Lua and LuaJava sources are included.

Building
--------

Assuming that `$SDK` points to your SDK and `$NDK` points to your NDK installation, run the following:

    git clone git://github.com/mkottman/AndroLua.git
    cd AndroLua
    $NDK/ndk-build 

This will build the native library, consisting of Lua and LuaJava. Then import the project into Eclipse, or run the following

    $SDK/tools/android update project -p .
    ant debug
    ant install

Usage
-----

The UI consist of the following:

* a large "Execute" button (that's what you want to do, after all)
* a text editor, where you can write Lua code, conveniently preloaded with the classic "Hello World!" example. A long click on the whole editor will clear it.
* a status/output window, that shows the output of 'print' function, and is scrollable should there be many lines of output

You can also work interactively by connecting to the TCP port 3333 of the device. You can do that either directly by using WiFi, or through the USB cable. For that you need to run the following:

    $SDK/platform-tools/adb forward tcp:3333 tcp:3333

In this version, there is a simple client `interp.lua` that uses LuaSocket. By default it will initially read stuff from `init.lua`.

For example:

	$ lua interp.lua
	loading init.lua
	
	> = activity
	sk.kottman.androlua.Main@405166c0
	> for i = 1,4 do print(i) end
	1
	2
	3
	4
