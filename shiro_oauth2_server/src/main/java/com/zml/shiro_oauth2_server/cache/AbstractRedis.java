package com.zml.shiro_oauth2_server.cache;

import com.zml.shiro_oauth2_server.cache.*;
import com.zml.shiro_oauth2_server.tools.CoreCodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 功能：redis的公共抽象类。
 * Charles on 2017/9/5.
 */
public abstract class AbstractRedis implements ICacheClient {

	private                Logger _log                 = LoggerFactory.getLogger(AbstractRedis.class);
	protected static final String DEFAULT_CHARSET_UTF8 = "UTF-8";
	protected String ipPorts;
	protected String password;
	//常用默认配置
	protected int maxTotal = 500;
	protected int maxIdle  = 20;
	protected int maxWait  = 10000;
	protected int timeout  = 10000;
	protected String prefixKey;//前缀key

	@Override
	public void destroy() throws Exception {
		//redis.close();
	}

	/**
	 * Redis初始化方法
	 *
	 * @throws Exception
	 */
	public abstract void init() throws Exception;

	protected String getDecodePass() {
		if (StringUtils.isNotBlank(password)) {
			try {
				return CoreCodecUtils.AESDecode(password, CoreCodecUtils.AES_DEFAULT_SEED);
			} catch (Exception e) {
				_log.error("Redis密码解密异常");
				throw new RuntimeException("Redis密码解密异常", e);
			}
		}
		return null;
	}

	/**
	 * 获取组合key
	 *
	 * @param key
	 * @return
	 */
	protected String getKey(String key) {
		String pk = getPrefixKey();
		if (pk == null) {
			return "" + key;
		}
		return pk + key;
	}

	protected String getPrefixKey() {
		return prefixKey;
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
}
