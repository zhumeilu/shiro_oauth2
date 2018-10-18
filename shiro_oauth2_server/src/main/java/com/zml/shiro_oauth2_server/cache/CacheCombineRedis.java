package com.zml.shiro_oauth2_server.cache;

import com.zml.shiro_oauth2_server.cache.impl.RedisClusterClient;
import com.zml.shiro_oauth2_server.cache.impl.RedisSingleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Set;

/**
 * 功能：redis单机版和集群版的集合，各实现类需要继承此类后使用
 * Charles on 2017/9/8.
 */
public abstract class CacheCombineRedis implements ICacheClient, InitializingBean {

	private static final Logger _log = LoggerFactory.getLogger(CacheCombineRedis.class);
	private   ICacheClient redisClient;
	protected String       ipPorts;
	protected String       password;
	//常用默认配置
	protected int maxTotal  = 500;
	protected int maxIdle   = 20;
	protected int maxWait   = 10000;
	protected int timeout   = 10000;
	protected int defaultDB = 0;


	/**
	 * 各业务前缀key
	 *
	 * @return
	 */
	protected abstract String getPrefixKey();

	@Override
	public void afterPropertiesSet() throws Exception {
		_log.info("DEFALUT_DB:" + defaultDB);
		if (this.ipPorts == null) {
			throw new RuntimeException("请初始化ip及端口信息");
		}
		if (ipPorts.contains(",")) {
			redisClient = new RedisClusterClient(ipPorts, password, maxTotal, maxIdle, maxWait, timeout, getPrefixKey());
		} else {
			redisClient = new RedisSingleClient(ipPorts, password, maxTotal, maxIdle, maxWait, timeout, getPrefixKey());
			redisClient.selectDB(defaultDB);
		}
	}


	@Override
	public void selectDB(Integer selectDB) {
		redisClient.selectDB(selectDB);
	}

	/**
	 * @return
	 * @throws Exception
	 */
	@Override
	public <Client> Client getClient() throws Exception {
		return redisClient.getClient();
	}

	@Override
	public void destroy(Object client) throws Exception {
		redisClient.destroy(client);
	}

	@Override
	public void destroy() throws Exception {
		redisClient.destroy();
	}
	@Override
	public String setString(String key, String value) {
		return redisClient.setString(key,value);
	}

	@Override
	public String getString(String key) {
		return redisClient.getString(key);
	}

	@Override
	public <T> String set(String key, T value) {
		return redisClient.set(key, value);
	}

	@Override
	public <T> String set(String key, int TTL, T value) {
		return redisClient.set(key, TTL, value);
	}

	@Override
	public <T> T get(String key) {
		return redisClient.get(key);
	}

	@Override
	public String setJson(String key, String json) {
		return redisClient.setJson(key, json);
	}

	@Override
	public String setJson(String key, int TTL, String json) {
		return redisClient.setJson(key, TTL, json);
	}

	@Override
	public String getJson(String key) {
		return redisClient.getJson(key);
	}

	@Override
	public Long expire(String key, int TTL) {
		return redisClient.expire(key, TTL);
	}

	@Override
	public Long delete(String key) {
		return redisClient.delete(key);
	}

	@Override
	public boolean exists(String key) {
		return redisClient.exists(key);
	}

	@Override
	public Long clear(String key) {
		return redisClient.clear(key);
	}

	@Override
	public Set<String> keySet(String key) {
		return redisClient.keySet(key);
	}

	@Override
	public Long ttl(String key) {
		return redisClient.ttl(key);
	}

	@Override
	public Long incr(String key) {
		return redisClient.incr(key);
	}

	@Override
	public Long decr(String key) {
		return redisClient.decr(key);
	}

	@Override
	public Long sadd(String key, String value, int TTL) {
		return redisClient.sadd(key, value, TTL);
	}

	@Override
	public Long lpush(String key, String... strings) {
		return redisClient.lpush(key, strings);
	}

	@Override
	public Long lrem(String key, long count, String value) {
		return redisClient.lrem(key, count, value);
	}

	@Override
	public Long size() {
		return redisClient.size();
	}

	@Override
	public Long srem(String key, String... members) {
		return redisClient.srem(key, members);
	}

	@Override
	public Set<String> smembers(String key) {
		return redisClient.smembers(key);
	}

	@Override
	public Long scard(String key) {
		return redisClient.scard(key);
	}

	@Override
	public Long llen(String key) {
		return redisClient.llen(key);
	}

	@Override
	public List<String> lrange(String key, int offset, int limit) {
		return redisClient.lrange(key, offset, limit);
	}

	public String getIpPorts() {
		return ipPorts;
	}

	public void setIpPorts(String ipPorts) {
		this.ipPorts = ipPorts;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getMaxTotal() {
		return maxTotal;
	}

	public void setMaxTotal(int maxTotal) {
		this.maxTotal = maxTotal;
	}

	public int getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}

	public int getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(int maxWait) {
		this.maxWait = maxWait;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getDefaultDB() {
		return defaultDB;
	}

	public void setDefaultDB(int defaultDB) {
		this.defaultDB = defaultDB;
	}
}
