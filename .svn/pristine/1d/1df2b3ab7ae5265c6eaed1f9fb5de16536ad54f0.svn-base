package cn.com.gome.logic.test.dao;

import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;  
import redis.clients.jedis.JedisPool;  
import redis.clients.jedis.JedisPoolConfig;  

/** 
 * Redis工具类,用于获取RedisPool. 
 */  
public class JedisUtil  {
	static Logger log = Logger.getLogger(JedisUtil.class);
	private static JedisUtil instance = null;
	private static JedisPool pool = null;
	
	private final static String ip = "10.69.213.181";
	private final static int port = 7000;
//	private final static String pass = Global.REDIS_PASS;
	
//	private final static String ip = "192.168.134.242";
//	private final static int port = 6379;
//	private final static String pass = "jjmatch";
    public static JedisUtil getInstance() {
    	if(instance == null) {
    		instance = new JedisUtil();
    	}
        return instance;
    }
    
    static {
        if (pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            pool = new JedisPool(config, ip, port, 10*1000);
           
        }
    }
    
    /** 
     * 获取Redis实例. 
     * @return Redis工具类实例 
     */  
    public Jedis getJedis() {
    	Jedis jedis = pool.getResource();
    	jedis.select(0);
        return jedis;
    }  
  
    /** 
     * 释放redis实例到连接池. 
     * @param jedis redis实例 
     */
    public void releaseJedis(Jedis jedis) {
        if(jedis != null) {
        	pool.returnResource(jedis);
        }
    }

    public static void main(String[] args) throws Exception {
		JedisUtil util = JedisUtil.getInstance();
		Jedis jedis = util.getJedis();
		jedis.select(0);
//		String s = jedis.hget("10000107@xinge.com_2_fromSet", "00@xinge.com_00");
		//String str = jedis.hget("10485207@xinge.com_2_fromSet", "notification@xinge.com_notification");
		//System.out.println(str);
		jedis.flushDB();
		//jedis.hdel("8000@xinge.com_2_userSet", "9001@xinge.com_other");
		//long l = jedis.hincrBy("8000@xinge.com_2_other", "9001@xinge.com_other", 1L);
		//System.out.println(l);
		//l = jedis.hincrBy("8000@xinge.com_2_other", "9002@xinge.com_other", 1L);
		//System.out.println(l);
		//String s = jedis.hget("8000@xinge.com_2_other", "9001@xinge.com_other");
		//System.out.println(s);
		//jedis.hdel("8000@xinge.com_2_other", "9001@xinge.com_other");
		
		//long l = jedis.hincrBy("8000@xinge.com_2_other", "9001@xinge.com_other", -1L);
		//System.out.println(l);
//		String s = jedis.hget("100@xinge.com_4_fromSet", "00@xinge.com_00");
//		if(s == null) {
//			System.out.println(s);
//		}
		
		
//		//for(int i = 0; i <10; i++) {
//			//jedis.hset("liuxm", "10000", "dfafsf");
//			//jedis.hdel("liuxm", "10000");
//			long count = jedis.incr("count_count");
//			System.out.println(count);
//			count = jedis.decr("count_count");
//			System.out.println(count);
//			count = jedis.decr("count_count");
//			System.out.println(count);
//			//jedis.del(toUser+ "_" + fromUser + "_" + type);
//			
//			
//			//Thread.sleep(500);
//		//}
////		String str = jedis.get("count_count");
////		System.out.println(str);
//		jedis.del("count_count");
////		str = jedis.get("count_count");
////		System.out.println(str);
		util.releaseJedis(jedis);
		//Thread.sleep(50000);
	}
}
