--其中KEYS[1]为key，ARGV[1]为value
if redis.call('get', KEYS[1]) == ARGV[1] then
    return redis.call('del', KEYS[1])
end
return 0