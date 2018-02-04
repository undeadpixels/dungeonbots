--
-- Created by IntelliJ IDEA.
-- User: stewartcharles
-- Date: 2/3/18
-- Time: 10:30 AM
-- To change this template use File | Settings | File Templates.
--

local lvl = {}

lvl.init = function()
    -- TODO: Create Maze Map
    local t = true
    local f = false
    local base = {
        {f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f},
        {f,t,t,t,t,t,f,t,t,t,f,t,t,f,t,t,t,t,t,f},
        {f,t,f,t,f,t,f,t,f,t,f,f,f,f,t,f,f,f,t,f},
        {f,t,f,t,f,t,t,t,f,t,t,t,t,t,t,f,t,t,t,f},
        {f,t,f,t,f,t,f,t,f,f,f,f,f,f,f,f,t,f,f,f},
        {f,f,f,t,f,f,f,t,f,f,t,t,t,t,t,t,t,t,t,f},
        {f,t,t,t,t,t,f,f,f,f,t,f,f,f,f,f,f,f,f,f},
        {f,t,f,t,f,f,f,t,t,t,t,f,t,t,t,t,t,t,t,f},
        {f,t,f,t,f,t,t,t,f,t,f,f,t,f,f,f,f,f,t,f},
        {f,t,f,f,f,t,f,f,f,t,f,f,t,f,t,t,t,t,t,f},
        {f,t,t,t,t,t,f,t,f,t,t,t,t,f,t,t,t,t,t,f},
        {f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f,f},
    }
    local h = 12
    local w = 20
    world:setSize(w,h)
    for j = 1, h do
        for i = 1, w do
            if base[j][i] then
                world:setTile(i, j, tileTypes:getTile("floor"))
            else
                world:setTile(i, j, tileTypes:getTile("wall"))
            end
        end
    end
    local player = Player.new(world, 2, 2)
    world:setPlayer(player)
end

lvl.update = function()
    local x, y = world.getPlayer().position()
    if x == 19 and y == 11 then
        world.win()
    end
end

return lvl