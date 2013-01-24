local packages = {}
local append = table.insert
local new = luajava.new

-- SciTE requires this, if you want to see stdout immediately...

io.stdout:setvbuf 'no'
io.stderr:setvbuf 'no'

local function new_tostring (o)
   return o:toString()
end

local function call (t,...)
    local obj,stat
    if select('#',...) == 1 and type(select(1,...))=='table' then
        obj = make_array(t,select(1,...))
    else
        stat,obj = pcall(new,t,...)
        if not stat then
            print(debug.traceback())
            os.exit(1)
        end
    end
	getmetatable(obj).__tostring = new_tostring
	return obj
end

local function import_class (classname,packagename)
    local res,class = pcall(luajava.bindClass,packagename)
    if res then
        _G[classname] = class
        local mt = getmetatable(class)
        mt.__call = call
        return class
    end
end

local function massage_classname (classname)
    if classname:find('_') then
        classname = classname:gsub('_','$')
    end
    return classname
end

local globalMT = {
	__index = function(T,classname)
            classname = massage_classname(classname)
			for i,p in ipairs(packages) do
                local class = import_class(classname,p..classname)
                if class then return class end
			end
            error("import cannot find "..classname)
	end
}
setmetatable(_G, globalMT)

function import (package)
    local i = package:find('%.%*$')
    if i then -- a wildcard; put into the package list, including the final '.'
        append(packages,package:sub(1,i))
    else
        local classname = package:match('([%w_]+)$')
        if not import_class(classname,package) then
            error("cannot find "..package)
        end
    end
end

append(packages,'')

function proxy (classname,obj)
    classname = massage_classname(classname)
	-- if the classname contains dots it's assumed to be fully qualified
	if classname:find('.',1,true) then
		return luajava.createProxy(classname,obj)
	end
	-- otherwise, it must lie on the package path!
	for i,p in ipairs(packages) do
		local ok,res = pcall(luajava.createProxy,p..classname, obj)
		if ok then return res end
	end
	error ("cannot find "..classname)
end


function enum(e)
   --local e = o:GetEnumerator()
   return function()
      if e:hasMoreElements() then
        return e:nextElement()
     end
   end
end

function dump (t)
    for k,v in pairs(t) do
        print(k,v)
    end
end

function p (o)
    if type(o) == 'userdata' then
		local mt = getmetatable(o)
		if not mt.__tostring then
			return print('java:'..o:toString())
		end
	end
	print(type(o)..':'..tostring(o))
end

import 'java.lang.reflect.Array'

function make_array (Type,list)
    local len
    local init = type(list)=='table'
    if init then
        len = #list
    else
        len = list
    end
    local arr = Array:newInstance(Type,len)
    if init then
        for i,v in ipairs(list) do
            Array:set(arr,i-1,v)
        end
    end
    return arr
end


import 'java.lang.*'
import 'java.util.*'

