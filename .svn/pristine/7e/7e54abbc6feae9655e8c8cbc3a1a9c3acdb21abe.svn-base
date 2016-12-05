package cn.com.gome.logic.cache;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.client.Connection;
import cn.com.gome.logic.handler.IMsgHandler;

/**
 * 缓存工具类
 */
public class ServerCache {
	static Logger log = LoggerFactory.getLogger(ServerCache.class);

	// 存放业务处理类
	private static Map<Short, IMsgHandler> map = new ConcurrentHashMap<Short, IMsgHandler>();
	// 保存客户端Channel
	private static Map<String, Channel> channelMap = new ConcurrentHashMap<String, Channel>();
	// 保存response Channel
	private static Map<String, Channel> respChannelMap = new ConcurrentHashMap<String, Channel>();
	// 接入层
	private static Map<String, String> gateWayMap = new HashMap<String, String>();
	// 保存response Connection
	private static Map<String, ConcurrentHashMap<ChannelId, Connection>> channelPool = new ConcurrentHashMap<String, ConcurrentHashMap<ChannelId, Connection>>();

	public static void setBaseHandler(Short cmd, IMsgHandler handler) {
		log.info("[setBaseHandler] cmd=[{}],className=[{}]", cmd, handler.getClass().getName());
		map.put(cmd, handler);
	}

	public static IMsgHandler getBaseHandler(Short cmd) {
		log.info("[getBaseHandler] cmd=[{}]", cmd);
		return map.get(cmd);
	}

	// 获取所有的命令字
	public static Set<Short> listCmd() {
		return map.keySet();
	}

	public static void setChannel(String key, Channel channel) {
		log.info("[setChannel] key=[{}], channel id=[{}]", key, channel.id());
		channelMap.put(key, channel);
	}

	public static Channel getChannel(String key) {
		log.info("[getChannel] key=[{}]", key);
		return channelMap.get(key);
	}

	public static void removeChannel(String key) {
		log.info("[removeChannel] key=[{}]", key);
		channelMap.remove(key);
	}

	public static Map<String, Channel> getChannelMap() {
		return channelMap;
	}

	public static void setRespChannel(String key, Channel channel) {
		log.info("[setRespChannel] key=[{}], channel id=[{}]", key, channel.id());
		respChannelMap.put(key, channel);
	}

	public static Channel getRespChannel(String key) {
		log.info("[getRespChannel] key=[{}]", key);
		return respChannelMap.get(key);
	}

	public static void removeRespChannel(String key) {
		log.info("[removeRespChannel] key=[{}]", key);
		respChannelMap.remove(key);
	}

	public static Map<String, Channel> getRespChannelMap() {
		return respChannelMap;
	}

	public static Map<String, String> getGateWayMap() {
		return gateWayMap;
	}

	public static void setGateWayMap(Map<String, String> gateWayMap) {
		ServerCache.gateWayMap = gateWayMap;
	}

	/**
	 * 保存Connection
	 * 
	 * @param respKey
	 * @param connection
	 */
	public static void setConnection(String respKey, Connection connection) {
		ConcurrentHashMap<ChannelId, Connection> channels = channelPool.get(respKey);
		if (channels == null) {
			channels = new ConcurrentHashMap<ChannelId, Connection>();
		}
		Channel channel = connection.getChannel();
		channels.put(channel.id(), connection);
		channelPool.put(respKey, channels);
	}

	/**
	 * 获取Connection列表
	 * 
	 * @param respKey
	 * @return
	 */
	public static ConcurrentHashMap<ChannelId, Connection> getConnections(String respKey) {
		ConcurrentHashMap<ChannelId, Connection> channels = channelPool.get(respKey);
		return channels;
	}

	/**
	 * 获取Connection
	 * @param respKey
	 * @return
	 */
	public static Connection getConnection(String respKey, ChannelId channelId) {
		ConcurrentHashMap<ChannelId, Connection> channels = channelPool.get(respKey);
		if(channels != null) {
			return channels.get(channelId);
		}
		 
		return null;
	}
	
	/**
	 * 根据key删除Connection列表
	 * 
	 * @param respKey
	 */
	public static void removeConnections(String respKey) {
		channelPool.remove(respKey);
	}

	/**
	 * 删除Connection
	 * 
	 * @param respKey
	 * @param connection
	 */
	public static void removeConnection(String respKey, Connection connection) {
		ConcurrentHashMap<ChannelId, Connection> channels = channelPool.get(respKey);
		if (channels != null) {
			Channel channel = connection.getChannel();
			if (channel != null) {
				channels.remove(channel.id());
				channelPool.put(respKey, channels);
			}
		}
	}

	/**
	 * 删除Connection
	 * 
	 * @param respKey
	 * @param channelId
	 */
	public static void removeConnection(String respKey, ChannelId channelId) {
		ConcurrentHashMap<ChannelId, Connection> channels = channelPool.get(respKey);
		if (channels != null) {
			channels.remove(channelId);
			channelPool.put(respKey, channels);
		}
	}
	
	/**
	 * 获取response Connection pool
	 * @return
	 */
	public static Map<String, ConcurrentHashMap<ChannelId, Connection>> getConnectionPool() {
		return channelPool;
	}
}
