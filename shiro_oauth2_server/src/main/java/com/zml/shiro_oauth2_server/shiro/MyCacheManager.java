package com.zml.shiro_oauth2_server.shiro;

import com.zml.shiro_oauth2_server.cache.ICacheClient;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ConcurrentHashMap;

public class MyCacheManager implements CacheManager {
    private static ConcurrentHashMap<String,Cache> map = new ConcurrentHashMap();
    @Autowired
    private ICacheClient cacheClient;
    @Override
    public <K, V> Cache<K, V> getCache(String s) throws CacheException {

        Cache cache = map.get(s);

        if(cache==null){
            cache = new MyCache(cacheClient);
            map.put(s,cache);
        }
        return cache;
    }

}
