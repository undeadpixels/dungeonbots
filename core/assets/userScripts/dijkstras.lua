--Min heap. 
function intDivideBy2(number)
    if (number % 2 > 0) then return (number-1) / 2 end
    return number/2
end
pq = {}
PriorityQueue = {}
function PriorityQueue.peek() return pq[1] end 
function PriorityQueue.push(item)  
    local idx = #pq+1
    pq[idx] = item
    --Percolate up        
    local parentIdx = intDivideBy2(idx)        
    while parentIdx >= 1 do     
        --player:say("idx:" .. idx .. "   parentIdx:" .. parentIdx)
        if pq[parentIdx].priority <= pq[idx].priority then break end            
        pq[idx] = pq[parentIdx]
        pq[parentIdx] = item
        idx = parentIdx
        parentIdx = intDivideBy2(idx)
    end    
end
function PriorityQueue.pop()
    local result = pq[1]
    pq[1] = pq[#pq]
    pq[#pq] = nil
    
    --Percolate down
    local idx = 1
    while true do
        local childIdx = idx * 2
        child = pq[childIdx]
        if not child then --If no lefthand child, then percolation down is impossible.
            return result
        elseif pq[idx].priority <= child.priority then  --Can't go left, but can it percolate right?
            childIdx = childIdx + 1
            child = pq[childIdx]
            if not child or pq[idx].priority <= child.priority then return result end
        elseif pq[childIdx + 1] and child.priority > pq[childIdx+1].priority then --Could go left, but is right percolation better?
            childIdx = childIdx + 1
            child = pq[childIdx]
        end
        pq[childIdx] = pq[idx]
        pq[idx] = child
        idx = childIdx
    end    
    return result
end
local visited = {}
local goal_x, goal_y = world:getGoal()
local goal = nil
local x,y = player:position()
PriorityQueue.push( { x=x, y=y, str = (x .. "," .. y), priority = 0, origin = nil} )
local function tryAdd(origin, x, y)
    str = (x .. "," .. y)
    local v = visited[str]
    local newprio = origin.priority + 1
    --newprio = newprio + abs(x - goal_x) + abs(y - goal_y) - abs(origin.x - goal_x) - abs(origin.y - goal_y)
    if not v then                          --Never visited, so create the node.            
        if world:isBlocking(x,y) then return end  --Can't add if it's a wall.
        v = {x=x, y=y, str=str, priority=newprio, origin=origin}
        visited[str] = v
        PriorityQueue.push( v )            
    elseif v.priority > newprio then  --Better route to this node?            
        v.origin = origin
        v.priority = newprio
        PriorityQueue.push(v)            
    end
end
--Do the traversal
step = 0
while (#pq > 0) do
    local focus = PriorityQueue.pop()
    
    if (focus.x == goal_x and focus.y == goal_y) then goal = focus end        
    tryAdd(focus, focus.x, focus.y - 1)        
    tryAdd(focus, focus.x, focus.y + 1)        
    tryAdd(focus, focus.x - 1, focus.y)        
    tryAdd(focus, focus.x + 1, focus.y)
    
    step = step + 1
end   
--Organize the states in reverse order in a solution    
local soln = { [1] = goal }
while soln[#soln].origin do soln[#soln+1] = soln[#soln].origin end
--Finally, walk the solution.
for i = #soln, 2, -1 do
    this = soln[i]
    next = soln[i-1]
    if next.y < this.y then player:down()
    elseif next.y > this.y then player:up()
    elseif next.x < this.x then player:left()
    elseif next.x > this.x then player:right()
    else assert(false, "Invalid path.")  
    end      
end
