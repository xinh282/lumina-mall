local stock = redis.call('GET', KEYS[1])
if not stock or tonumber(stock) <= 0 then
    return -1
end

local hasBought = redis.call('EXISTS', KEYS[2])
if hasBought == 1 then
    return -2
end

local remaining = redis.call('DECR', KEYS[1])
redis.call('SETEX', KEYS[2], 3600, '1')
return remaining
