package com.zml.shiro_oauth2_server.cache;

import org.apache.logging.log4j.LogManager;

/**
 * 功能：
 * Charles on 2017/9/4.
 */
public final class RedisClient extends CacheCombineRedis {

	protected static final org.apache.logging.log4j.Logger _log = LogManager.getLogger(RedisClient.class);

	public RedisClient() {
		_log.info("CommonRedis初始化");
	}

	public RedisClient(String ipPorts,
                       String password,
                       Integer maxTotal,
                       Integer maxIdle,
                       Integer maxWait,
                       Integer timeout) {
		super.ipPorts = ipPorts;
		super.password = password;
		super.maxTotal = maxTotal;
		super.maxIdle = maxIdle;
		super.maxWait = maxWait;
		super.timeout = timeout;
		_log.info("CommonRedis初始化");
	}

	@Override
	protected String getPrefixKey() {
		return "";
	}
}
