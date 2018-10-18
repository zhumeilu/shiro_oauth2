package com.zml.shiro_oauth2_server.cache.impl;


import com.alibaba.fastjson.JSONObject;
import com.zml.shiro_oauth2_server.cache.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.util.*;

/**
 * @Description: redis集群解决方案
 * @author: chengxing
 * @date: 2016年7月28日
 * @version: V1.0
 */
public class RedisClusterClient extends AbstractRedis {

	private Logger       _log        = LoggerFactory.getLogger(RedisClusterClient.class);
	private int          maxAttempts = 5;
	private JedisCluster redis       = null;

	public RedisClusterClient(String ipPorts,
			String password,
			//常用默认配置
			int maxTotal,
			int maxIdle,
			int maxWait,
			int timeout,
			String prefixKey
	) throws Exception {
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
	}

	/**
	 * redis集群
	 *
	 * @return
	 */
	@Override
	public JedisCluster getClient() throws Exception {
		if (this.redis == null) {
			init();
		}
		return redis;
	}

	/**
	 * 初始化方法
	 *
	 * @throws Exception
	 */
	@Override
	public void init() throws Exception {
		_log.info("#####Redis集群初始化START#####");
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
			if (StringUtils.isNotEmpty(ipPorts)) {
				String[] ipArr = null;
				if (ipPorts.contains(",")) {
					ipArr = ipPorts.split(",");
				}
				if (ipArr == null) {
					throw new RuntimeException("Redis缓存初始化异常:ip格式异常请参照{ip:端口[,ip:端口]}形式进行设置");
				}
				Set<HostAndPort> ljis = new HashSet<HostAndPort>();
				for (String redisNode : ipArr) {
					if (StringUtils.isNotBlank(redisNode)
							&& redisNode.contains(":")) {
						String[] redisAddr = redisNode.split(":");
						if (Utils.isNumber(redisAddr[1])) {
							HostAndPort hp = new HostAndPort(redisAddr[0],
									Integer.parseInt(redisAddr[1]));
							ljis.add(hp);
						}
					}
				}
				if (ljis.isEmpty()) {
					throw new RuntimeException("Redis缓存初始化异常:ip格式异常请参照{ip:端口[,ip:端口]}形式进行设置");
				}
				redis = new JedisCluster(ljis, timeout, 3000, maxAttempts, tPass, config);
			} else {
				throw new RuntimeException("Redis缓存初始化异常:ip不能为空");
			}
		} catch (Exception e) {
			_log.error("公共模块", "Redis缓存初始化", e);
			throw new RuntimeException("Redis缓存初始化异常", e);
		}
		_log.info("#####Redis集群初始化END#####");
	}

	public void destroy(Object redis) throws Exception {
	}

	/**
	 * Get方法, 转换结果类型并屏蔽异常,仅返回Null.
	 */
	@Override
	public <T> T get(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		try {
			byte[] x = getClient().get(getKey(key).getBytes(DEFAULT_CHARSET_UTF8));
			if (x == null) {
				return null;
			}
			CacheObject<T> co = (CacheObject<T>) SerializeUtil.unserialize(x);
			if (co != null) {
				return co.getObject();
			}
		} catch (Exception e) {
			e.printStackTrace();
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.get key=" + getKey(key),
					e);
		}
		return null;
	}

	@Override
	public String getString(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		try {
			String x = getClient().get(getKey(key));
			return x;
		} catch (Exception e) {
			e.printStackTrace();
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.get key=" + getKey(key),
					e);
		}
		return null;
	}

	@Override
	public boolean exists(String key) {
		boolean result = false;
		if (StringUtils.isBlank(key)) {
			return false;
		}
		try {
			result = getClient().exists(getKey(key));
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.exists key="
					+ getKey(key), e);
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
		try {
			String x = getClient().get(getKey(key));
			if (StringUtils.isBlank(x)) {
				return null;
			}
			CacheJson co = JSONObject.parseObject(x, CacheJson.class);
			if (co != null) {
				return co.getJson();
			}
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.get key=" + getKey(key),
					e);
		}
		return null;
	}

	@Override
	public <T> String set(String key, T value) {
		return set(key, 0, value);
	}

	@Override
	public String setString(String key, String value) {
		return setString(key,0,value);
	}

	private String setString(String key,int TTL,String value){
		if (value == null || StringUtils.isBlank(key)) {
			return null;
		}
		try {
			if (TTL == 0) {
				return getClient().set(getKey(key), value);
			} else {
				return getClient().setex(getKey(key), TTL, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.set key=" + key,
					e);
			return null;
		}
	}
	@Override
	public <T> String set(String key, int TTL, T value) {
		if (value == null || StringUtils.isBlank(key)) {
			return null;
		}
		try {
			CacheObject<T> co = new CacheObject<T>();
			co.setObject(value);
			co.setKey(getKey(key));
			if (TTL == 0) {
				return getClient().set(getKey(key).getBytes(DEFAULT_CHARSET_UTF8), SerializeUtil.serialize(co));
			} else {
				return getClient().setex(getKey(key).getBytes(DEFAULT_CHARSET_UTF8), TTL, SerializeUtil.serialize(co));
			}
		} catch (Exception e) {
			e.printStackTrace();
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.set key=" + key,
					e);
			return null;
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
		try {
			CacheJson cj = new CacheJson();
			cj.setJson(json);
			cj.setKey(getKey(key));
			if (TTL == 0) {
				return getClient().set(getKey(key), JSONObject.toJSONString(cj));
			} else {
				return getClient().setex(getKey(key), TTL, JSONObject.toJSONString(cj));
			}
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.set key=" + getKey(key),
					e);
			return null;
		}
	}

	@Override
	public Long delete(String key) {
		if (StringUtils.isBlank(key)) {
			return 0L;
		}
		try {
			return getClient().del(getKey(key));
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.delete key="
					+ getKey(key), e);
			return 0L;
		}
	}

	/**
	 * @return
	 */
	@Override
	public synchronized Long clear(String key) {
		Set<String> keysSet;
		try {
			Long tt = 0L;
			keysSet = getClient().hkeys(getKey(key));
			if (keysSet != null && !keysSet.isEmpty()) {
				tt += redis.del(keysSet.toArray(new String[] {}));
			}
			return tt;
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.clear key=" + getKey(key), e);
			return 0L;
		}
	}

	@Override
	public Set<String> keySet(String key) {
		Set<String> keys = new TreeSet<>();
		try {
			Map<String, JedisPool> clusterNodes = getClient().getClusterNodes();
			for (String k : clusterNodes.keySet()) {
				_log.debug("Getting keys from: {}", k);
				JedisPool jp = clusterNodes.get(k);
				Jedis connection = jp.getResource();
				try {
					keys.addAll(connection.keys(getKey(key)));
				} catch (Exception e) {
					_log.error("Getting keys error: {}", e);
				} finally {
					_log.debug("Connection closed.");
					connection.close();//用完一定要close这个链接！！！
				}
			}
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.keySet key="
					+ getKey(key), e);
			keys = Collections.emptySet();
		}
		return keys;
	}

	@Override
	public Long expire(String key, int TTL) {
		if (StringUtils.isBlank(key)) {
			return 0L;
		}
		try {
			return getClient().expire(getKey(key), TTL);
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.expire key="
					+ getKey(key), e);
			return 0L;
		}
	}

	@Override
	public Long ttl(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		try {
			return getClient().ttl(getKey(key).getBytes(DEFAULT_CHARSET_UTF8));
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.ttl key=" + getKey(key),
					e);
		}
		return null;
	}

	@Override
	public Long incr(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		try {
			return getClient().incr(getKey(key));
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.incr key=" + getKey(key), e);
		}
		return null;
	}

	@Override
	public Long decr(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		try {
			return getClient().decr(getKey(key));
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.decr key=" + getKey(key), e);
		}
		return null;
	}

	@Override
	public Long sadd(String key, String value, int TTL) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		try {
			Long sadd = getClient().sadd(getKey(key), value);
			getClient().expire(getKey(key), TTL);
			return sadd;
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.sadd key=" + getKey(key), e);
		}
		return null;
	}

	@Override
	public Long lpush(String key, String... strings) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		try {
			Long lpush = getClient().lpush(getKey(key), strings);
			return lpush;
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.incr sadd=" + getKey(key), e);
		}
		return null;
	}

	@Override
	public Long lrem(String key, long count, String value) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		try {
			Long lrem = getClient().lrem(getKey(key), count, value);
			return lrem;
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.lrem lrem=" + getKey(key), e);
		}
		return null;
	}

	@Override
	public Long srem(String key, String... members) {
		if (StringUtils.isBlank(key)) {
			return 0L;
		}
		try {
			Long srem = getClient().srem(getKey(key), members);
			return srem;
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.srem key=" + getKey(key), e);
		}
		return 0L;
	}

	@Override
	public Set<String> smembers(String key) {
		if (StringUtils.isBlank(key)) {
			return Collections.emptySet();
		}
		try {
			return getClient().smembers(getKey(key));
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.smembers key=" + getKey(key), e);
		}
		return Collections.emptySet();
	}

	@Override
	public Long scard(String key) {
		if (StringUtils.isBlank(key)) {
			return 0L;
		}
		try {
			return getClient().scard(getKey(key));
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.scard key=" + getKey(key), e);
		}
		return 0L;
	}

	@Override
	public Long llen(String key) {
		if (StringUtils.isBlank(key)) {
			return 0L;
		}
		try {
			return getClient().llen(getKey(key));
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.llen key=" + getKey(key), e);
		}
		return 0L;
	}

	@Override
	public List<String> lrange(String key, int offset, int limit) {
		if (StringUtils.isBlank(key)) {
			return Collections.emptyList();
		}
		try {
			return getClient().lrange(getKey(key), offset, limit);
		} catch (Exception e) {
			_log.error(BasicCode.PUBLIC_CACHE, "RedisClusterClient.llen key=" + getKey(key), e);
		}
		return Collections.emptyList();
	}

	@Override
	public Long size() {
		return 0L;
	}

	public static void main(String[] args) {
		//		JedisPoolConfig config = new JedisPoolConfig();
		//		config.setMaxTotal(1000);
		//		config.setMaxIdle(20);
		//		config.setMaxWaitMillis(1000);
		//		String[] hp = new String[] { "192.168.1.89:7000",
		//				"192.168.1.89:7001", "192.168.1.89:7002",
		//				"192.168.1.89:7003", "192.168.1.89:7004",
		//				"192.168.1.89:7005" };
		//		Set<HostAndPort> ljis = new HashSet<HostAndPort>();
		//		for (String s : hp) {
		//			String[] sArr = s.split(":");
		//			HostAndPort hp1 = new HostAndPort(sArr[0], Integer.parseInt(sArr[1]));
		//			ljis.add(hp1);
		//		}
		//		JedisCluster pool = new JedisCluster(ljis, config);
		//		RedisClusterClient client = new RedisClusterClient();
		//		client.setRedis(pool);
		//		client.set("Test", 1200, "Test");
		//		System.out.println("" + client.get("Test"));
		//		try {
		//			client.destroy();
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//		}
		//		System.out.println("" + client.get("Test"));
	}
}