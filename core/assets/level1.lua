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

tutorial_1, tutorial_2, tutoria_3 = false, false, false

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
    world:setTile(6,6, tileTypes:getTile("goal"))
    world:setTile(4,4, tileTypes:getTile("grass"))
    
    world:makeBot("bot1", 10, 3)
    world:makeBot("bot2", 10, 4)
    world:makeBot("bot3", 10, 5)
end

tbl.update = function()
    local x, y = world:getPlayer():position()
    local gx, gy = world:getGoal()
    if x == gx and y == gy then
        world:win()
    elseif ((x == 2 and y == 2) or (x == 2 and y == 2)) and not tutorial_1 then
        tutorial_1 = true
        world:alert("Welcome to Dungeon Bots\n"
                .. "To move your player invoke any of the following methods!\n"
                .. "- player:up();\n- player:left();\n- player:right();\n- player:down()\n"
                .. "You can also invoke 'player:up(5)' with a number of spaces to move!","Tutorial 1")
    elseif ((x == 2 and y == 3) or (x == 3 and y == 2)) and not tutorial_2 then
        tutorial_2 = true
        world:alert("Get to the Goal\nTo ask for the position of the Goal input\n\treturn world:getGoal()","Tutorial 2")
    elseif (x == 4 and y == 4) and not tutorial_3 then
        tutorial_3 = true
        world:openBrowser('https://youtu.be/MRPpYX8SOA8')
    end
end

tbl.init()
registerUpdateListener(tbl.update)


return tbl
