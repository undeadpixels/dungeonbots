-- Level 2
-- Djikstra's Maze
-- Created by Stewart Charles
-- 2/3/2018

local lvl = {}
-- Example metadata attached to each level
lvl.name = "Level 2"
lvl.author = "Stewart Charles"
lvl.init = function()
    -- Define map size
    local h = 12
    local w = 20
    world:setSize(w,h)

    -- Create map
    local F = tileTypes:getTile("floor")
    local W = tileTypes:getTile("wall")
    local base = {
        { W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W },
        { W, F, F, F, F, F, W, F, F, F, W, F, F, W, F, F, F, F, F, W },
        { W, F, W, F, W, F, W, F, W, F, W, W, W, W, F, W, W, W, F, W },
        { W, F, W, F, W, F, F, F, W, F, F, F, F, F, F, W, F, F, F, W },
        { W, F, W, F, W, F, W, F, W, W, W, W, W, W, W, W, F, W, W, W },
        { W, W, W, F, W, W, W, F, W, W, F, F, F, F, F, F, F, F, F, W },
        { W, F, F, F, F, F, W, W, W, W, F, W, W, W, W, W, W, W, W, W },
        { W, F, W, F, W, W, W, F, F, F, F, W, F, F, F, F, F, F, F, W },
        { W, F, W, F, W, F, F, F, W, F, W, W, F, W, W, W, W, W, F, W },
        { W, F, W, W, W, F, W, W, W, F, W, W, F, W, F, F, F, F, F, W },
        { W, F, F, F, F, F, W, F, W, F, F, F, F, W, F, F, F, F, F, W },
        { W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W, W },
    }

    -- Set Tiles
    for j = 1, h do
        for i = 1, w do
            world:setTile(i, j, base[j][i])
        end
    end

    -- Add player and goal
    local player = Player.new(world, 2, 2)
    player:setDefaultCode("player:right()")
    player:setStats(1,2,3,4)
    world:setPlayer(player)
end

lvl.update = function()
    local x, y = world.getPlayer().position()
    if x == 19 and y == 11 then
        world.win()
    end
end

return lvl