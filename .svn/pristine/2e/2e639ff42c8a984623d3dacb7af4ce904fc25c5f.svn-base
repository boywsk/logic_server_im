package cn.com.gome.logic.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import cn.com.gome.logic.global.Global;

/**
 * MQ资讯类消息(客服)(生产)发送者
 */
public class ConsultMQSender {
	static Logger log = LoggerFactory.getLogger(ConsultMQSender.class);
	// final static String QUEUE_NAME = Global.CUSTOMER_CUSTOMER;
	final static String EXCHANGE_NAME = "customerServiceDirect";
	private static Channel channel = null;
	private static Connection connection = null;
	private static ConsultMQSender instance = null;

	static {
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(Global.MQ_HOST);
			factory.setPort(Global.MQ_PORT);
			factory.setUsername(Global.MQ_USERNAME);
			factory.setPassword(Global.MQ_PASSWORD);
			factory.setVirtualHost(Global.MQ_VIRTUALHOST);
			factory.setAutomaticRecoveryEnabled(true);
			factory.setConnectionTimeout(5000);
			factory.setRequestedHeartbeat(5);
			connection = factory.newConnection();
			channel = connection.createChannel();
			// 声明转发器和类型
			channel.exchangeDeclare(EXCHANGE_NAME, "direct");
			// channel.queueDeclare(QUEUE_NAME, true, false, false, null);
		} catch (Exception e) {
			log.error("[init Channel]:", e);
			close();
		}
	}

	private ConsultMQSender() {

	}

	public static synchronized ConsultMQSender getInstance() {
		if (instance == null) {
			instance = new ConsultMQSender();
		}
		return instance;
	}

	/**
	 * 发送消息
	 * 
	 * @param msg
	 */

//	public void sendMsg(byte[] msg) {
//		log.info("[sendMsg]......");
//		try {
//			if (channel == null || connection == null) {
//				close();
//				reInitChannel();
//			}
//			channel.basicPublish("", QUEUE_NAME, null, msg);
//		} catch (Exception e) {
//			close();
//			log.error("[sendMsg] error cause:", e);
//		}
//	}

	/**
	 * 发送消息-direct
	 * 
	 * @param msg
	 * @param uid
	 *            指客服id,目前仅有：9999999999，9999999997
	 */
	public void sendMsg(byte[] msg, long uid) {
		log.info("[sendMsg]......");
		try {
			if (channel == null || connection == null) {
				close();
				reInitChannel();
			}
			channel.basicPublish(EXCHANGE_NAME, String.valueOf(uid), null, msg);
		} catch (Exception e) {
			close();
			log.error("[sendMsg] error cause:", e);
		}
	}

	/**
	 * 关闭
	 * 
	 * @param msg
	 */
	public static void close() {
		try {
			if (channel != null) {
				channel.close();
			}
			if (connection != null) {
				connection.close();
			}
		} catch (Exception e) {
			log.error("close:", e);
		} finally {
			channel = null;
			connection = null;
		}
	}

	private void reInitChannel() {
		try {
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(Global.MQ_HOST);
			factory.setPort(Global.MQ_PORT);
			factory.setUsername(Global.MQ_USERNAME);
			factory.setPassword(Global.MQ_PASSWORD);
			factory.setVirtualHost(Global.MQ_VIRTUALHOST);
			factory.setAutomaticRecoveryEnabled(true);
			factory.setConnectionTimeout(5000);
			factory.setRequestedHeartbeat(5);
			connection = factory.newConnection();
			channel = connection.createChannel();
			// 声明转发器和类型
			channel.exchangeDeclare(EXCHANGE_NAME, "direct");
			// channel.queueDeclare(QUEUE_NAME, true, false, false, null);
		} catch (Exception e) {
			log.error("[reInitChannel]:", e);
			close();
		}
	}

}
