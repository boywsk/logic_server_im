package cn.com.gome.logic.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;
import cn.com.gome.logic.model.ServerResource;
import cn.com.gome.logic.utils.IPUtils;
import cn.com.gome.logic.utils.JedisUtils;

/**
 * 服务器资源redis操作工具类
 */
public class ServerResurceRedisDao {
	Logger log = LoggerFactory.getLogger(ServerResurceRedisDao.class);
	
	/**
	 * 保存服务器资源信息
	 * @param resource
	 */
	public void saveServerResource(String serverType, ServerResource resource) {
		String jsonRes = JSON.toJSONString(resource);
		log.info("[saveServerResource] jsonRes:[{}]", jsonRes);
		JedisUtils util = JedisUtils.getInstance();
//		jedis.select(Global.SERVER_RESOURCE_REDIS_INDEX);
		try {
			String ip = resource.getServerIp();
			int port = resource.getServerPort();
			String key = IPUtils.ipToLong(ip) + ":" + port;
			util.getJedisCluster().hset(serverType, key, jsonRes);
		} catch(Exception e) {
			log.error("[saveServerResource]error,cause is {}",e);
		}
//		finally {
//			util.releaseJedis(jedis);
//		}
		
	}
}
