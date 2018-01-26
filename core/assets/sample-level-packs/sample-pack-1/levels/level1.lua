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




function init()
    world.setSize(16, 16)
    for i in 1,16 do
        for j in 1,16 do
            if i == 1 or i == 16 or j == 1 or j == 16 then
                world.setTile(j, i, tileTypes.getTile("wall"))
            else
                world.setTile(j, i, tileTypes.getTile("floor"))
            end
        end
    end
    world.setTile(Tile.goal(14, 14))

    world.setPlayer(Player.new(world,2, 2))
    --world.player:setCode("autobind()")
    --settings.allowedCode.whitelist("autobind")
    whitelist.allow(player, SecurityLevel.DEBUG)
    --settings.playStyle.codeMode = "none"
    --settings.playStyle.playMode = "rts"
end

function update(dt)
    local x, y = world.getPlayer().position()
    if x == 14 and y == 14 then
        world.win()
    end
end
