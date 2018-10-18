package com.zml.shiro_oauth2_server.cache.impl;

import com.alibaba.fastjson.JSONObject;
import com.zml.shiro_oauth2_server.cache.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * redis单机版实现
 */
public class RedisSingleClient extends AbstractRedis {

	private              Logger  _log               = LoggerFactory.getLogger(RedisSingleClient.class);
	private              Integer DefaultDB          = 0;//单机版默认数据库
	private static final Integer DEFAULT_REDIS_PORT = 6379;
	private JedisPool pool;

	public RedisSingleClient(String ipPorts,
			String password,
			//常用默认配置
			int maxTotal,
			int maxIdle,
			int maxWait,
			int timeout,
			String prefixKey) throws Exception {
		this.ipPorts = ipPorts;
		this.password = password;
		this.maxTotal = maxTotal;
		this.maxIdle = maxIdle;
		this.maxWait = maxWait;
		this.timeout = timeout;
		super.prefixKey = prefixKey;
		init();
	}

	@Override
	public void selectDB(Integer selectDB) {
		this.DefaultDB = selectDB;
	}

	/**
	 * 获取单机版Redis
	 *
	 * @return
	 */
	@Override
	public Jedis getClient() throws Exception {
		if (this.pool != null) {
			return this.pool.getResource();
		} else {
			init();
		}
		return this.pool.getResource();
	}

	/**
	 * 初始化方法
	 *
	 * @throws Exception
	 */
	@Override
	public void init() throws Exception {
		_log.info("#####Redis单机初始化START#####");
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(maxTotal);
		config.setMaxIdle(maxIdle);
		config.setMaxWaitMillis(maxWait);
		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);
		String tPass;
		try {
			//获取解密密码
			tPass = getDecodePass();
			_log.info("IP:" + ipPorts + ",Pass:" + tPass);
			if (StringUtils.isNotBlank(ipPorts)) {
				if (ipPorts.contains(":")) {
					String[] redisAddr = ipPorts.split(":");
					if (Utils.isNumber(redisAddr[1])) {
						pool = new JedisPool(config, redisAddr[0], Integer.parseInt(redisAddr[1]), timeout, tPass);
					} else {
						throw new RuntimeException("Redis端口号格式异常:" + redisAddr[1]);
					}
				} else {
					pool = new JedisPool(config, ipPorts, DEFAULT_REDIS_PORT, timeout, tPass);
				}
			} else {
				throw new RuntimeException("Redis缓存初始化异常:ip不能为空");
			}
		} catch (Exception e) {
			_log.error("公共模块", "Redis缓存初始化", e);
			throw new RuntimeException("Redis缓存初始化异常", e);
		}
		_log.info("#####Redis单机初始化END#####");
	}

	/**
	 * Get方法, 转换结果类型并屏蔽异常,仅返回Null.
	 */
	@Override
	public <T> T get(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			byte[] x = redis.get(getKey(key).getBytes(DEFAULT_CHARSET_UTF8));
			if (x == null) {
				return null;
			}
			CacheObject<T> co = (CacheObject<T>) SerializeUtil.unserialize(x);
			if (co != null) {
				return co.getObject();
			}
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.get key=" + getKey(key), e);
		} finally {
			if (redis != null)
				redis.close();
		}
		return null;
	}


	@Override
	public String getString(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			String x = redis.get(getKey(key));
			return x;
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.get key=" + getKey(key), e);
		} finally {
			if (redis != null)
				redis.close();
		}
		return null;
	}

	@Override
	public boolean exists(String key) {
		boolean result = false;
		if (StringUtils.isBlank(key)) {
			return false;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			result = redis.exists(getKey(key));
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.exists key=" + getKey(key), e);
			result = false;
		} finally {
			if (redis != null)
				redis.close();
		}
		return result;
	}

	/**
	 * 获取存储的JSON数据
	 */
	@Override
	public String getJson(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		String result = null;
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			String x = redis.get(getKey(key));
			if (StringUtils.isBlank(x)) {
				return null;
			}
			CacheJson co = JSONObject.parseObject(x, CacheJson.class);
			if (co != null) {
				return co.getJson();
			}
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.get key=" + getKey(key), e);
		} finally {
			if (redis != null)
				redis.close();
		}
		return null;
	}

	public void destroy(Object redis) throws Exception {
		if (redis != null)
			((Jedis) redis).close();
	}

	@Override
	public <T> String set(String key, T value) {
		return set(key, 0, value);
	}

	@Override
	public String setString(String key, String value) {

		return set(key,0,value);
	}

	@Override
	public <T> String set(String key, int TTL, T value) {
		if (value == null || StringUtils.isBlank(key)) {
			return null;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			CacheObject<T> co = new CacheObject<T>();
			co.setObject(value);
			co.setKey(getKey(key));
			if (TTL == 0) {
				return redis.set(getKey(key).getBytes(DEFAULT_CHARSET_UTF8), SerializeUtil.serialize(co));
			} else {
				return redis.setex(getKey(key).getBytes(DEFAULT_CHARSET_UTF8), TTL, SerializeUtil.serialize(co));
			}
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.set key=" + getKey(key), e);
			return null;
		} finally {
			if (redis != null)
				redis.close();
		}
	}

	private String set(String key, int TTL, String value) {
		if (value == null || StringUtils.isBlank(key)) {
			return null;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			if (TTL == 0) {
				return redis.set(getKey(key), value);
			} else {
				return redis.setex(getKey(key), TTL, value);
			}
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.set key=" + getKey(key), e);
			return null;
		} finally {
			if (redis != null)
				redis.close();
		}
	}

	/**
	 * JSON格式存储
	 *
	 * @param key
	 * @param json
	 * @return
	 */
	@Override
	public String setJson(String key, String json) {
		return set(key, 0, json);
	}

	/**
	 * JSON格式存储
	 *
	 * @param key
	 * @param TTL  秒
	 * @param json
	 * @return
	 */
	@Override
	public String setJson(String key, int TTL, String json) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			CacheJson cj = new CacheJson();
			cj.setJson(json);
			cj.setKey(getKey(key));
			if (TTL == 0) {
				return redis.set(getKey(key), JSONObject.toJSONString(cj));
			} else {
				return redis.setex(getKey(key), TTL, JSONObject.toJSONString(cj));
			}
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.set key=" + getKey(key), e);
			return null;
		} finally {
			if (redis != null)
				redis.close();
		}
	}

	@Override
	public Long delete(String key) {
		if (StringUtils.isBlank(key)) {
			return 0L;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			return redis.del(getKey(key));
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.delete key=" + getKey(key), e);
			return 0L;
		} finally {
			if (redis != null)
				redis.close();
		}
	}

	/**
	 * @return
	 */
	@Override
	public Long clear(String key) {
		Jedis redis = null;
		Set<String> keysSet;
		try {
			Long tt = 0L;
			redis = getClient();
			redis.select(getDefaultDB());
			keysSet = redis.keys(getKey(key));
			if (keysSet != null && !keysSet.isEmpty()) {
				tt += redis.del(keysSet.toArray(new String[]{}));
			}
			return tt;
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.clear key=" + getKey(key), e);
			return 0L;
		} finally {
			if (redis != null)
				redis.close();
		}
	}

	@Override
	public Set<String> keySet(String key) {
		Jedis redis = null;
		Set<String> keysSet;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			keysSet = redis.keys(getKey(key));
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.keySet pattern=" + getKey(key), e);
			keysSet = Collections.emptySet();
		} finally {
			if (redis != null)
				redis.close();
		}
		return keysSet;
	}

	@Override
	public Long expire(String key, int TTL) {
		if (StringUtils.isBlank(key)) {
			return 0L;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			return redis.expire(getKey(key), TTL);
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.expire key=" + getKey(key), e);
		} finally {
			if (redis != null)
				redis.close();
			return 0L;
		}
	}

	@Override
	public Long ttl(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			return redis.ttl(key.getBytes(DEFAULT_CHARSET_UTF8));
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.ttl key=" + key, e);
			return null;
		} finally {
			if (redis != null)
				redis.close();
		}
	}

	@Override
	public Long incr(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			return redis.incr(key);
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.incr key=" + key, e);
		} finally {
			if (redis != null)
				redis.close();
		}
		return null;
	}

	@Override
	public Long decr(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			return redis.decr(key);
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.decr key=" + key, e);
		} finally {
			if (redis != null)
				redis.close();
		}
		return null;
	}

	@Override
	public Long sadd(String key, String value, int TTL) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			Long sadd = redis.sadd(key, value);
			redis.expire(key, TTL);
			return sadd;
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.sadd key=" + key, e);
		} finally {
			if (redis != null)
				redis.close();
		}
		return null;
	}

	@Override
	public Long lpush(String key, String... strings) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			return redis.lpush(key, strings);
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.lpush key=" + key, e);
		} finally {
			if (redis != null)
				redis.close();
		}
		return null;
	}

	@Override
	public Long lrem(String key, long count, String value) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			return redis.lrem(key, count, value);
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.lrem key=" + key, e);
		} finally {
			if (redis != null)
				redis.close();
		}
		return null;
	}

	@Override
	public Long size() {
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			return redis.dbSize();
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.size ", e);
		} finally {
			if (redis != null)
				redis.close();
		}
		return 0L;
	}

	@Override
	public Long srem(String key, String... members) {
		if (StringUtils.isBlank(key)) {
			return 0L;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			Long srem = redis.srem(key, members);
			return srem;
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.srem key=" + key, e);
		} finally {
			if (redis != null)
				redis.close();
		}
		return 0L;
	}

	@Override
	public Set<String> smembers(String key) {
		if (StringUtils.isBlank(key)) {
			return Collections.emptySet();
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			return redis.smembers(key);
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.smembers key=" + key, e);
		} finally {
			if (redis != null)
				redis.close();
		}
		return Collections.emptySet();
	}

	@Override
	public Long scard(String key) {
		if (StringUtils.isBlank(key)) {
			return 0L;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			return redis.scard(key);
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.scard key=" + key, e);
		} finally {
			if (redis != null)
				redis.close();
		}
		return 0L;
	}

	@Override
	public Long llen(String key) {
		if (StringUtils.isBlank(key)) {
			return 0L;
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			return redis.llen(key);
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.llen key=" + key, e);
		} finally {
			if (redis != null)
				redis.close();
		}
		return 0L;
	}

	@Override
	public List<String> lrange(String key, int offset, int limit) {
		if (StringUtils.isBlank(key)) {
			return Collections.emptyList();
		}
		Jedis redis = null;
		try {
			redis = getClient();
			redis.select(getDefaultDB());
			return redis.lrange(key, offset, limit);
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisSingleClient.llen key=" + key, e);
		} finally {
			if (redis != null)
				redis.close();
		}
		return Collections.emptyList();
	}

	public Integer getDefaultDB() {
		return DefaultDB;
	}

	public static void main(String[] args) {
		//		ShardedJedisPool pp = null;
		//		JedisShardInfo s = null;
		//		ShardedJedis r1 = null;
		//		RedisSingleClient client = new RedisSingleClient();
		//		JedisPoolConfig config = new JedisPoolConfig();
		//		config.setMaxTotal(100);
		//		config.setMaxIdle(20);
		//		config.setMaxWaitMillis(1000);
		//		config.setTestOnBorrow(true);
		//		JedisPool pool = new JedisPool(config, "10.10.235.163", 7001);
		//		Random r = new Random(1000000);
		//		String key = "test";
		//		// Map<String, String> smap = client.get("testusr");
		//		// if(smap==null){
		//		// smap=new HashMap<String,String>();
		//		// }
		//		// // String value = smap.get("login.smap.except.user");
		//		// smap.put("user", "bbb;aa;");
		//		// client.set("testusr", smap);
		//		client.set(key, "564545446546546");
		//		// client.delete("aaaa");
		//		//		for (int i = 0; i < 3192; i++) {
		//		//			System.out.println(i);
		//		//			client.set("test" + i, 3600, r.nextInt());
		//		//		}
		//		//		client.delete("testusr");
		//		//		 int smapres = client.get(key);
	}
}