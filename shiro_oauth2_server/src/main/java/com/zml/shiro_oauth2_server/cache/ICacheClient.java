package com.zml.shiro_oauth2_server.cache;

import java.util.List;
import java.util.Set;

/**
 * Cache统一接口
 */
public interface ICacheClient {

	/**
	 * 获取客户端,若需要调用客户端进行特殊方法调用<br/>，需要主动调用destroy(Object client);方法进行连接释放
	 *
	 * @param <Client>
	 * @return
	 * @throws Exception
	 */
	<Client> Client getClient() throws Exception;

	/**
	 * 针对获取客户端getClient()方法提供的关闭连接方法
	 *
	 * @param client
	 * @throws Exception
	 */
	void destroy(Object client) throws Exception;

	void selectDB(Integer selectDB);

	void destroy() throws Exception;

	/**
	 * 保存序列化数据
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	<T> String set(String key, T value);

	String setString(String key, String value);

	/**
	 * 保存有有效期的数据
	 *
	 * @param key
	 * @param value
	 * @param value 数据超时的秒数
	 * @return
	 */
	<T> String set(String key, int TTL, T value);

	/**
	 * 获取序列化的缓存数据
	 *
	 * @param key
	 * @return
	 */
	<T> T get(String key);

	String getString(String key);

	/**
	 * 保存JSON化数据
	 *
	 * @param key
	 * @param json
	 * @return
	 */
	String setJson(String key, String json);

	/**
	 * 保存JSON数据
	 *
	 * @param key
	 * @param json
	 * @return
	 */
	String setJson(String key, int TTL, String json);

	/**
	 * 获取JSON化的缓存数据
	 *
	 * @param key
	 * @return
	 */
	String getJson(String key);

	/**
	 * 重置超时时间
	 *
	 * @param key
	 * @param TTL 数据超时的秒数
	 * @return
	 */
	Long expire(String key, int TTL);

	/**
	 * 移出缓存数据
	 *
	 * @param key
	 * @return
	 */
	Long delete(String key);

	/**
	 * 缓存中是否存在key
	 *
	 * @param key
	 * @return
	 */
	boolean exists(String key);

	/**
	 * 删除通过key（可加*通配查询）查询到的所有key的缓存数据
	 *
	 * @return
	 */
	Long clear(String key);

	/**
	 * 缓存所有的key的集合
	 *
	 * @return
	 */
	Set<String> keySet(String key);

	/**
	 * 缓存key的剩余生命周期
	 *
	 * @return
	 */
	Long ttl(String key);

	/**
	 * incr
	 *
	 * @param key
	 * @return 失败返回null，成功返回加1后的值
	 */
	Long incr(String key);

	/**
	 * decr
	 *
	 * @param key
	 * @return 失败返回null，成功返回减1后的值
	 */
	Long decr(String key);

	/**
	 * set add
	 *
	 * @param key
	 * @param value
	 * @param TTL
	 * @return 成功返回添加数，失败返回null
	 */
	Long sadd(String key, String value, int TTL);

	/**
	 * list add
	 *
	 * @param key
	 * @param strings
	 * @return 成功返回添加数，失败返回null
	 */
	Long lpush(String key, String... strings);

	/**
	 * list remove
	 *
	 * @param key
	 * @param count
	 * @param value
	 * @return 成功返回移除数，失败返回null
	 */
	Long lrem(String key, long count, String value);

	/**
	 * @return
	 */
	Long size();

	/**
	 * @return
	 */
	Long srem(String key, String... members);

	/**
	 * 获取key的set集合
	 *
	 * @param key
	 * @return
	 */
	Set<String> smembers(String key);

	/**
	 * @param key
	 * @return
	 */
	Long scard(String key);

	/**
	 * @param key
	 * @return
	 */
	Long llen(String key);

	/**
	 * @param key
	 * @param offset
	 * @param limit
	 * @return
	 */
	List<String> lrange(String key, int offset, int limit);
}