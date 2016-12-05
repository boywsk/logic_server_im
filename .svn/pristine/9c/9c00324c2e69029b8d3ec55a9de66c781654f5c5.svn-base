package cn.com.gome.logic.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.global.Global;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis工具类,用于获取RedisPool.
 */
public class JedisUtils {
	static Logger log = LoggerFactory.getLogger(JedisUtils.class);

	private static JedisUtils instance = null;
	private static JedisCluster jc = null;

	// private static JedisPool pool = null;
	// private final static String ip = Global.REDIS_ADDRESS;
	// private final static int port = Global.REDIS_PORT;
	// private final static String pass = Global.REDIS_PASS;

	private final static String cluster_address = Global.REDIS_CLUSTER_ADDRESS;

	public static JedisUtils getInstance() {
		if (instance == null) {
			instance = new JedisUtils();
		}
		return instance;
	}

	static {
		log.info("cluster_address=[{}]", cluster_address);
		String[] addressArr = cluster_address.split(";");
		Set<HostAndPort> hps = new HashSet<HostAndPort>();
		for (String str : addressArr) {
			String[] hostArr = str.split(":");
			String ip = hostArr[0];
			int port = Integer.valueOf(hostArr[1]);
			HostAndPort hp = new HostAndPort(ip, port);
			hps.add(hp);
		}
		JedisPoolConfig config = new JedisPoolConfig();
		// 超时，最大的转发数，最大链接数，最小链接数都会影响到集群
		jc = new JedisCluster(hps, 5000, 10, config);

		// if (pool == null) {
		// //config.setTestOnBorrow(true);
		// if(pass != null && pass.length() > 0) {
		// pool = new JedisPool(config, ip, port, 10*1000, pass);
		// } else {
		// pool = new JedisPool(config, ip, port, 10*1000);
		// }
		//
		// }
	}

	public JedisCluster getJedisCluster() {
		if (jc == null) {
			log.info("cluster_address=[{}]", cluster_address);
			String[] addressArr = cluster_address.split(";");
			Set<HostAndPort> hps = new HashSet<HostAndPort>();
			for (String str : addressArr) {
				String[] hostArr = str.split(":");
				String ip = hostArr[0];
				int port = Integer.valueOf(hostArr[1]);
				HostAndPort hp = new HostAndPort(ip, port);
				hps.add(hp);
			}
			JedisPoolConfig config = new JedisPoolConfig();
			// 超时，最大的转发数，最大链接数，最小链接数都会影响到集群
			jc = new JedisCluster(hps, 5000, 10, config);
		}

		return jc;
	}

	/**
	 * // * 获取Redis实例. // * @return Redis工具类实例 //
	 */
	// public Jedis getJedis() {
	// Jedis jedis = pool.getResource();
	// return jedis;
	// }
	//
	// /**
	// * 释放redis实例到连接池.
	// * @param jedis redis实例
	// */
	// public void releaseJedis(Jedis jedis) {
	// if(jedis != null) {
	// //pool.returnResource(jedis);
	// jedis.close();
	// }
	// }
	//
	public static void main(String[] args) throws Exception {
		// String clientIdStr = String.valueOf((byte)10);
		// String ss = clientIdStr.substring(0, 1);
		JedisUtils util = JedisUtils.getInstance();
//		util.getJedisCluster().del("groupSeqId");
//		boolean b = util.getJedisCluster().exists("gomeplus_test_groupSeqId_mx29631463726626697");//583_585 TEST_APP_ID_groupSeqId
//		System.out.println(b);
//		long inc = util.getJedisCluster().incr("groupSeqId");
//		System.out.println(inc);
//		String str = util.getJedisCluster().get("gomeplus_test_groupSeqId_mx29631463726626697");//585_587
//		System.out.println(Long.valueOf(str));  IM_LOGIC_PRODUCE
		
//		boolean b = util.getJedisCluster().exists("IM_LOGIC_PRODUCE");
//		System.out.println(b);
		Map<String, String> map = util.getJedisCluster().hgetAll("IM_LOGIC_PRODUCE");
		System.out.println(map.size());
		for(String key : map.keySet()) {
			String[] keyArr = key.split(":");
			System.out.println(key);
			String ip = IPUtils.longToIP(Long.parseLong(keyArr[0]));
			System.out.println(ip);
		}
		
//		String str = map.get("175976737:8000");
//		System.out.println(str);
//		util.getJedisCluster().hdel("IM_LOGIC_PRODUCE", "175976737:8000");
		
//		boolean b = util.getJedisCluster().exists("im-platform-im-server-url-key");
//		System.out.println(b);
//		String str = util.getJedisCluster().get("im-platform-im-server-url-key");
//		System.out.println(str);
//		util.getJedisCluster().del("im-platform-im-server-url-key");
//		int size = util.getJedisCluster().getClusterNodes().size();
//		System.out.println(size);
//		Set<String> keys =  util.getJedisCluster().getClusterNodes().keySet();
//		System.out.println(keys.size());
		
//		Map<String, String> map = util.getJedisCluster().hgetAll("IM_LOGIC_TEST");
//		System.out.println(map.size());
		
		// Jedis jedis = util.getJedis();
		// jedis.select(0);
		// jedis.setex("1000_11", 30, "1:2");
		// System.out.println(jedis.keys("1000_1*"));
		// //System.out.println(jedis.exists("1000_10"));
		// //String s = jedis.hget("10000107@xinge.com_2_fromSet",
		// "00@xinge.com_00");
		// //String str = jedis.hget("10485207@xinge.com_2_fromSet",
		// "notification@xinge.com_notification");
		// //System.out.println(str);
		// //jedis.flushDB();
		// //jedis.hdel("8000@xinge.com_2_userSet", "9001@xinge.com_other");
		// //long l = jedis.hincrBy("8000@xinge.com_2_other",
		// "9001@xinge.com_other", 1L);
		// //System.out.println(l);
		// //l = jedis.hincrBy("8000@xinge.com_2_other", "9002@xinge.com_other",
		// 1L);
		// //System.out.println(l);
		// //String s = jedis.hget("8000@xinge.com_2_other",
		// "9001@xinge.com_other");
		// //System.out.println(s);
		// //jedis.hdel("8000@xinge.com_2_other", "9001@xinge.com_other");
		//
		// //long l = jedis.hincrBy("8000@xinge.com_2_other",
		// "9001@xinge.com_other", -1L);
		// //System.out.println(l);
		//// String s = jedis.hget("100@xinge.com_4_fromSet",
		// "00@xinge.com_00");
		//// if(s == null) {
		//// System.out.println(s);
		//// }
		//
		//
		//// //for(int i = 0; i <10; i++) {
		//// //jedis.hset("liuxm", "10000", "dfafsf");
		//// //jedis.hdel("liuxm", "10000");
		//// long count = jedis.incr("count_count");
		//// System.out.println(count);
		//// count = jedis.decr("count_count");
		//// System.out.println(count);
		//// count = jedis.decr("count_count");
		//// System.out.println(count);
		//// //jedis.del(toUser+ "_" + fromUser + "_" + type);
		////
		////
		//// //Thread.sleep(500);
		//// //}
		////// String str = jedis.get("count_count");
		////// System.out.println(str);
		//// jedis.del("count_count");
		////// str = jedis.get("count_count");
		////// System.out.println(str);
		// util.releaseJedis(jedis);
		// //Thread.sleep(50000);
	}
}
