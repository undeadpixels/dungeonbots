--[[
A sample level to test our parsing and stuff
]]

--[[
stuff that's passed in:

playStyle
 - codeMode     "repl", "editor", "none"
 - playMode     "rts", "teamTurns", "turns"

allowedCode
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

tiles
 - floor
 - wall
 - goal
 - ???


]]

function init()
    world.tiles:setSize(16, 16)
    for i in 1,16 do
        for j in 1,16 do
            if i == 1 or i == 16 or j == 1 or j == 16 then
                world.tiles.setTile(j, i, tiles.wall)
            else
                world.tiles.setTile(j, i, tiles.floor)
            end
        end
    end
    world.tiles.setTile(14, 14, tiles.goal)

    world.player = Player.Player(2, 2)
    world.player:setCode("autobind()")

    allowedCode.whitelist("autobind")

    playStyle.codeMode = "none"
    playStyle.playMode = "rts"
end

function update(dt)
    if world.player.x == 14 and world.player.y == 14 then
        world.win(nil)
    end
end
