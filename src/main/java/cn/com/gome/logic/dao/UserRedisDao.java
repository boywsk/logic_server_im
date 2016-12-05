package cn.com.gome.logic.dao;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import cn.com.gome.logic.utils.IPUtils;
import cn.com.gome.logic.utils.JedisUtils;
import redis.clients.jedis.JedisCluster;

/**
 * 用户在线信息redis操作工具类
 */
public class UserRedisDao {

	private static Logger log = LoggerFactory.getLogger(UserRedisDao.class);
	
	/**
	 * 根据用户id获取用户token
	 * @param appId
	 * @param uid
	 * @return
	 */
	public String getPlatform(String appId, long uid) {
		log.info("[getUserToken] appId=[{}],uid=[{}]", appId, uid);
		JedisUtils util = JedisUtils.getInstance();
		String key = appId + "_" + uid + "_PLATFORM_SUFFIX";
		String platform = util.getJedisCluster().get(key);
		return platform;
	}

	/**
	 * 根据用户id获取用户接入层数据
	 * @param appId
	 * @param uid
	 * @return
	 */
	public Map<String, String> listUserRsp(String appId, long uid) {
		log.info("[listUserRsp] appId=[{}],uid=[{}]", appId, uid);
		JedisUtils util = JedisUtils.getInstance();
//		Jedis jedis = util.getJedis();
//		jedis.select(Global.REDIS_INDEX);
		try {
			String key = appId + "_" + uid;
			Map<String, String> map = util.getJedisCluster().hgetAll(key);
			log.info("[listUserRsp] uid=[{}],result map size=[{}]", uid, map == null ? 0 : map.size());
			return map;
		} catch (Exception e) {
			log.error("[listUserRsp] cause is:", e);
		}
//		finally {
//			util.releaseJedis(jedis);
//		}
		log.info("[listUserRsp] uid=[{}],result back null", uid);

		return null;
	}

	/**
	 * 删除某个用户某个终端类型的接入层数据
	 * @param appId
	 * @param uid
	 * @param clientId
	 * @param token
	 */
	public void delUserRsp(String appId, long uid, byte clientId, String deviceId) {
		log.info("[delUserRsp] appId=[{}],uid=[{}],clientId=[{}],deviceId=[{}]", appId, uid, clientId, deviceId);
		JedisUtils util = JedisUtils.getInstance();
		try {
			String key = appId  + "_" + uid;
			String fild = clientId  + "_" + deviceId;
			util.getJedisCluster().hdel(key, fild);
		} catch (Exception e) {
			log.error("[delUserRsp] cause is:", e);
		}
	}

	/**
	 * 保存某个用户某个终端类型的接入层数据
	 * @param appId
	 * @param uid
	 * @param clientId
	 * @param token
	 * @param rspInfo
	 */
	public void saveUserRsp(String appId, long uid, byte clientId, String deviceId, String rspInfo) {
		log.info("[saveUserRsp] appId=[{}],uid=[{}],clientId=[{}],deviceId=[{}]", appId, uid, clientId, deviceId);
		log.info("[saveUserRsp]  rspInfo=[{}]", rspInfo);
		JedisUtils util = JedisUtils.getInstance();
//		Jedis jedis = util.getJedis();
//		jedis.select(Global.REDIS_INDEX);
		try {
			String key = appId  + "_" + uid;
			String fild = clientId  + "_" + deviceId;
			util.getJedisCluster().hset(key, fild, rspInfo);
		} catch (Exception e) {
			log.error("[saveUserRsp]  cause is:", e);
		}
//		finally {
//			util.releaseJedis(jedis);
//		}
	}
	
	/**
	 * 获取用户终端类型
	 * @param appId
	 * @param uId
	 * @return
	 */
	public int getUserTerminalType(String appId, long uid) {
		int type = 1;
		JedisCluster cluster = JedisUtils.getInstance().getJedisCluster();
		try {
			String key = appId + "_" + uid + "_PLATFORM_SUFFIX";
			String value = cluster.get(key);
			log.info("[getUserTerminalType] key=[{}],value=[{}]", key, value);
			if(!Strings.isNullOrEmpty(value)) {
				type = Integer.parseInt(value);
			}
		} catch (Exception e) {
			log.error("[getUserTerminalType] cause is:", e);
		}
		
		return type;
	}

//	/**
//	 * 设置用户终端device token
//	 * 
//	 * @param uid
//	 * @param clientId
//	 * @param diviceToke
//	 */
//	public void saveDeviceToken(long uid, byte clientId, String deviceToken) {
//		log.info("uid=[{}],clientId=[{}],diviceToke=[{}]", uid, clientId, deviceToken);
//		JedisUtils util = JedisUtils.getInstance();
//		Jedis jedis = util.getJedis();
//		jedis.select(Global.REDIS_INDEX + 1);
//		try {
//			jedis.hset("" + uid, "" + clientId, deviceToken);
//			String data = getData(deviceToken);
//			String key = uid + "_" + clientId;
//			if (data != null) {
//				if (!data.equals(key)) {
//					delDeviceToken(uid, clientId);
//					// 保存信息
//					saveData(uid, clientId, deviceToken);
//				}
//			} else {
//				// 保存信息
//				saveData(uid, clientId, deviceToken);
//			}
//		} catch (Exception e) {
//			log.error("cause is:", e);
//		} finally {
//			util.releaseJedis(jedis);
//		}
//	}
//
//	/**
//	 * 获取用户终端device token
//	 * 
//	 * @param uid
//	 * @param clientId
//	 * @param diviceToke
//	 */
//	public String getDeviceToken(long uid, byte clientId) {
//		log.info("uid=[{}],clientId=[{}]", uid, clientId);
//		String deviceToken = null;
//		JedisUtils util = JedisUtils.getInstance();
//		Jedis jedis = util.getJedis();
//		jedis.select(Global.REDIS_INDEX + 1);
//		try {
//			deviceToken = jedis.hget("" + uid, "" + clientId);
//		} catch (Exception e) {
//			log.error("cause is:", e);
//		} finally {
//			util.releaseJedis(jedis);
//		}
//
//		return deviceToken;
//	}
//
//	/**
//	 * 删除用户终端device token
//	 * 
//	 * @param uid
//	 * @param clientId
//	 */
//	public void delDeviceToken(long uid, byte clientId) {
//		log.info("auid=[{}],clientId=[{}]", uid, clientId);
//		JedisUtils util = JedisUtils.getInstance();
//		Jedis jedis = util.getJedis();
//		jedis.select(Global.REDIS_INDEX + 1);
//		try {
//			jedis.hdel("" + uid, "" + clientId);
//			if (jedis.hlen(String.valueOf(uid)) <= 0) {
//				jedis.hdel(String.valueOf(uid));
//			}
//		} catch (Exception e) {
//			log.error("cause is:", e);
//		} finally {
//			util.releaseJedis(jedis);
//		}
//	}
//
//	/**
//	 * deviceToken为key保存信息
//	 * 
//	 * @param uid
//	 * @param clientId
//	 * @param deviceToken
//	 */
//	public void saveData(long uid, byte clientId, String deviceToken) {
//		log.info("uid=[{}],clientId=[{}],diviceToke=[{}]", uid, clientId, deviceToken);
//		JedisUtils util = JedisUtils.getInstance();
//		Jedis jedis = util.getJedis();
//		jedis.select(Global.REDIS_INDEX + 2);
//		try {
//			String data = uid + "_" + clientId;
//			jedis.set(deviceToken, data);
//		} catch (Exception e) {
//			log.error("cause is:", e);
//		} finally {
//			util.releaseJedis(jedis);
//		}
//	}
//
//	/**
//	 * deviceToken为key取信息
//	 * 
//	 * @param deviceToken
//	 * @return
//	 */
//	public String getData(String deviceToken) {
//		log.info("diviceToke=[{}]", deviceToken);
//		String data = null;
//		JedisUtils util = JedisUtils.getInstance();
//		Jedis jedis = util.getJedis();
//		jedis.select(Global.REDIS_INDEX + 2);
//		try {
//			data = jedis.get(deviceToken);
//		} catch (Exception e) {
//			log.error("cause is:", e);
//		} finally {
//			util.releaseJedis(jedis);
//		}
//
//		return data;
//	}
//
//	/**
//	 * deviceToken为key删除信息
//	 * 
//	 * @param deviceToken
//	 * @return
//	 */
//	public void delData(String deviceToken) {
//		log.info("diviceToke=[{}]", deviceToken);
//		JedisUtils util = JedisUtils.getInstance();
//		Jedis jedis = util.getJedis();
//		jedis.select(Global.REDIS_INDEX + 2);
//		try {
//			jedis.del(deviceToken);
//		} catch (Exception e) {
//			log.error("cause is:", e);
//		} finally {
//			util.releaseJedis(jedis);
//		}
//	}

	/**
	 * push计数加1
	 * @param appId
	 * @param uid
	 * @param apnsToken
	 * @return
	 */
	public long incPushCount(String appId, long uid, String apnsToken) {
		log.info("[incPushCount] appId=[{}],uid=[{}],apnsToken=[{}]", appId, uid, apnsToken);
		long count = 0L;
		JedisUtils util = JedisUtils.getInstance();
//		Jedis jedis = util.getJedis();
//		util.getJedisCluster().select(Global.REDIS_INDEX + 3);
		try {
			count = util.getJedisCluster().hincrBy("apns_" + appId + "＿" +uid, apnsToken, 1L);
		} catch (Exception e) {
			log.error("[incPushCount] cause is:", e);
		}
//		finally {
//			util.releaseJedis(jedis);
//		}

		return count;
	}

	/**
	 * 清除push计数
	 * @param appId
	 * @param uid
	 * @param apnsToken
	 */
	public void cleanPushCount(String appId, long uid, String apnsToken) {
		log.info("[cleanPushCount] appId=[{}],uid=[{}],apnsToken=[{}]", appId, uid, apnsToken);
		JedisUtils util = JedisUtils.getInstance();
//		Jedis jedis = util.getJedis();
//		jedis.select(Global.REDIS_INDEX + 3);
		try {
			util.getJedisCluster().hdel("apns_" + appId + "_" + uid, apnsToken);
		} catch (Exception e) {
			log.error("[cleanPushCount] cause is:", e);
		}
//		finally {
//			util.releaseJedis(jedis);
//		}
	}
	
	public static void main(String[] args) {
//		byte clientId = (byte)1;
		UserRedisDao dao = new UserRedisDao();
//		dao.saveUserRsp("TEST_APP_ID", 100000L, clientId, "AAAAAAA", "rspInfo");
		Map<String, String> map = dao.listUserRsp("gomeplus_test", 3585);
		for(String key : map.keySet()) {
			String value = map.get(key);
			System.out.println("key=" + key);
			System.out.println("value=" + value);
			String[] arr = value.split(":");
			long l = Long.valueOf(arr[0]);
			System.out.println("ip=" + IPUtils.longToIP(l));
		}
//		dao.delUserRsp("TEST_APP_ID", 100000L, clientId, "AAAAAAA");
	}
}
