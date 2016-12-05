package cn.com.gome.logic.global;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;
import cn.com.gome.logic.utils.StringUtils;

/**
 * 存放文件配置信息或者全局变量配置信息
 */
public class Global {
	static Logger log = LoggerFactory.getLogger(Global.class);

	// 服务系统账号
	public static int SERVER_ID;
	// 服务器ip
	public static String SERVER_IP;
	// 服务器端口
	public static int SERVER_PORT = 9000;
	// 心跳检测；单位秒
	public static int HEARTBEAT_TIME = 300;
	// 接入层验证crc
	public static int GATEWAY_CRC;
	// handler class path
	public static String HANDLER_CLASSPATH;

	// 包头长度
	public static short PACK_HEAD_LENGTH = 96;
	// appId 长度
	public static short APPID_LENGTH = 32;
	// 包起始标识
	public static byte HEAD_START_TAG = 0x2;
	// 包起始标识
	public static byte HEAD_END_TAG = 0x3;
	public static String REDIS_CLUSTER_ADDRESS;
	// 服务器资源每个多久上报一次；Quartz配置;分钟
	public static String SERVER_RESOURCE_EXPIRE;
	//response client pool size
	public static int RESPONSE_POOL_SIZE = 5;
	
	// 消息队列host
	public static String MQ_HOST;
	// 消息队列port
	public static int MQ_PORT;
	// 消息队列virtualHost
	public static String MQ_VIRTUALHOST;
	// 消息队列账号
	public static String MQ_USERNAME;
	// 消息队列密码
	public static String MQ_PASSWORD;
	// 消息队列名称
	public static String MQ_QUEUENAME;
	// apns消息推送队列名称
	public static String APNS_QUEUENAME;
	//客服消息队列名;客服消息生产（客服代理是生产者）队列
	public static String CUSTOMER_PRODUCER;
	//#客服消息队列名;客服消息消费（客服代理是消费者）队列
	public static String CUSTOMER_CUSTOMER;
	
	// APNS证书存放路径
	public static String APNS_EKYSTORE_PATH;
	// 证书存密钥
	public static String APNS_EKYSTORE_PASSWORD;
	// 聊天消息分库模值
	public static int MSG_DB_MODULO;
	// 聊天消息分表模值
	public static int MSG_TABLE_MODULO;

	// mongodb数据库名称
	public static String DB_NAME;
	// mongodb 离线消息数据库名称
	public static String DB_MSG_NAME;

	// 服务资源redis上报key
	public static String SERVER_RESOURCE;

	// zookeeper连接地址;多个用逗号(,)分开
	public static String ZOOKEEPER_ADDRESS;
	// gk zookeeper根
	public static String ZOOKEEPER_ROOT;
	//接入层服务地址和端口列表;key:longIp:prot,value:""
	public static Map<String, String> GATEWAY_ADDRESS = new ConcurrentHashMap<String, String>();
	//调度服务地址和端口；多个用分好分割；如：ip:port;ip:port
	public static List<String> DISPATCHER_ADDRESS = new ArrayList<String>();
	//分包后最大包长度
	public static final int MAX_PACK_SIZE = 16000;
	//分包后最小包长度
//	public static final int MIN_PACK_SIZE = 20000;
		
	static {
		log.info("[load static]start...");
		try {
			Configuration config = new PropertiesConfiguration(StringUtils.getRealPath() + "/config/config.properties");
			SERVER_ID = config.getInt("server.id");
			SERVER_IP = config.getString("server.ip", null);
			SERVER_PORT = config.getInt("server.port");
			HEARTBEAT_TIME = config.getInt("server.heartbeat.time", 300);
			GATEWAY_CRC = config.getInt("gateway.crc", 0);
			HANDLER_CLASSPATH = config.getString("server.handler.classPath", null);
			SERVER_RESOURCE_EXPIRE = config.getString("server.resource.expire", "");
			REDIS_CLUSTER_ADDRESS = config.getString("logic.server.redis.cluster.address", "");
			
			MQ_HOST = config.getString("mq.host");
			MQ_PORT = config.getInt("mq.port");
			MQ_VIRTUALHOST = config.getString("mq.virtualHost");
			MQ_USERNAME = config.getString("mq.username");
			MQ_PASSWORD = config.getString("mq.password");
			MQ_QUEUENAME = config.getString("mq.queueName");
			APNS_QUEUENAME = config.getString("mq.apns.queuName");
			CUSTOMER_PRODUCER = config.getString("mq.customer-queueName-pro");
			CUSTOMER_CUSTOMER = config.getString("mq.customer-queueName-con");
			APNS_EKYSTORE_PATH = config.getString("apns.keystore.path");
			APNS_EKYSTORE_PASSWORD = config.getString("apns.keystore.password");
			MSG_DB_MODULO = config.getInt("msg.db.modulo", 64);
			MSG_TABLE_MODULO = config.getInt("msg.table.modulo", 2);
			DB_NAME = config.getString("db_name");
			DB_MSG_NAME = config.getString("db_msg_name");
			SERVER_RESOURCE = config.getString("server.resource.key");
			ZOOKEEPER_ADDRESS = config.getString("zookeeper.address");
			ZOOKEEPER_ROOT = config.getString("zookeeper.gk.root");
			String addressStr = config.getString("dispatcher.address", "");
			log.info("dispatcher.address=[{}]", addressStr);
			if(!Strings.isNullOrEmpty(addressStr)) {
				String[] arrAdress = addressStr.split(";");
				for(String str : arrAdress) {
					DISPATCHER_ADDRESS.add(str);
				}
			}
			RESPONSE_POOL_SIZE = config.getInt("response.pool.size", 5);	
		} catch (ConfigurationException e) {
			log.error("", e);
		}
		
		log.info("[load static]end...");
	}
	
	public static void main(String[] args) {
		System.out.println(Global.GATEWAY_ADDRESS.size());
	}
}
