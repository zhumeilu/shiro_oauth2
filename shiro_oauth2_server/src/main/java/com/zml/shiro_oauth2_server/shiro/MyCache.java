package com.zml.shiro_oauth2_server.shiro;

import com.zml.shiro_oauth2_server.cache.ICacheClient;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class MyCache implements Cache {
    Logger logger = LoggerFactory.getLogger(MyCache.class);
    private static final String prefixStr = "shiro_oauth2_cache_";
    private ICacheClient cacheClient;
    public MyCache(ICacheClient cacheClient){
        this.cacheClient = cacheClient;
    }

    public String getKey(Object o){
        logger.info("-----MyCache.getKey---------");
        return prefixStr+o.toString();
    }
    @Override
    public Object get(Object o) throws CacheException {
        logger.info("-----MyCache.get---------");
        return cacheClient.get(getKey(o));
    }

    @Override
    public Object put(Object o, Object o2) throws CacheException {
        logger.info("-----MyCache.put---------");
        return cacheClient.set(getKey(o),o2);
    }

    @Override
    public Object remove(Object o) throws CacheException {
        logger.info("-----MyCache.remove---------");
        return cacheClient.delete(getKey(o));
    }

    @Override
    public void clear() throws CacheException {
        logger.info("-----MyCache.clear---------");
        cacheClient.delete(getKey("")+"*");
    }

    @Override
    public int size() {
        logger.info("-----MyCache.size---------");
        return cacheClient.size().intValue();
    }

    @Override
    public Set keys() {
        logger.info("-----MyCache.keys---------");
        Set<String> strings = cacheClient.keySet(prefixStr + "*");

        return strings;
    }

    @Override
    public Collection values() {
        logger.info("-----MyCache.values---------");
        List list = new ArrayList();
        Set<String> strings = cacheClient.keySet(prefixStr + "*");
        strings.forEach(str->{
            list.add(cacheClient.get(str));
        });
        return list;
    }
}
