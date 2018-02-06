--[[
A sample level to test our parsing and stuff
]]

--[[
stuff that's passed in:

settings
 - playStyle
   - codeMode     "repl", "editor", "none"
   - playMode     "rts", "teamTurns", "turns"
   - autoStart    true / false
 - allowedCode
   - whitelist    function/class/whatever name
   - blacklist    function name

world
 - tiles        custom class
   - setSize      function(width, height)
   - setTile      function(x, y, Tile)
   - getTile      function(x, y, Tile)
 - bots         array of Actors
 - player       player reference
 - enemies      array of Actors
 - win          function(info)
 - listenFor    function(eventName, funcPtr)

tileTypes
 - floor
 - wall
 - goal
 - ???
]]

local tbl = { }

tbl.init = function()

    world:setSize(16, 16)
    for i = 1,16 do
        for j = 1,16 do
            if i == 1 or i == 16 or j == 1 or j == 16 then
                world:setTile(j, i, tileTypes:getTile("wall"))
            else
                world:setTile(j, i, tileTypes:getTile("floor"))
            end
        end
    end
    local player = Player.new(world, 2, 2)
    world:setPlayer(player)
    world:setGoal(5,5)
end

tbl.update = function()
    local x, y = world:getPlayer():position()
    if x == 5 and y == 5 then
        world:win()
    end
end

return tbl
