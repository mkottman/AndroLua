require 'socket'
local c = socket.connect('localhost',3333)
local log = io.open('log.txt','a')

function readfile(file)
  local f,err = io.open(file)
  if not f then return nil,err end
  local contents = f:read '*a':gsub('\n','\001')
  f:close()
  return contents
end

function eval(line)
  c:send(line..'\n')
  local res = c:receive()
  return res:gsub('\001','\n')
end

local init,err = readfile 'init.lua'
if init then
  print 'loading init.lua'
  io.write(eval(init)..'\n')
end

io.write '> '
local line = io.read()

while line do
  log:write(line,'\n')
  local cmd,file = line:match '^%.(.)%s+(.+)$'
  if file then
    local mod
    if cmd == 'm' then
      mod = file
      file = mod:gsub('%.','/')..'.lua'
    end
    line,err = readfile(file)
    if mod and line then
      line = '--mod:'..mod..'\001'..line
    end
  else
    local expr = line:match '^%s*=%s*(.+)$'
    if expr then
      line = 'print('..expr..')'
    end
  end
  if line then
    local res = eval(line)
    log:write(res,'\n')
    io.write(res)
  else
    print(err)
  end
  io.write '> '
  line = io.read()
end
log:close()
c:close()
