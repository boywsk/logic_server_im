package cn.com.gome.logic.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import cn.com.gome.logic.global.Global;

public class MQSender {
	static Logger log = LoggerFactory.getLogger(MQSender.class);
	
	final static String APNS_QUEUE_NAME = Global.APNS_QUEUENAME;
	
	private static Channel channel = null;
	private static Connection connection = null;
	private static MQSender instance = null;
	
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
			channel.queueDeclare(APNS_QUEUE_NAME, true, false, false, null);
		} catch (Exception e) {
			log.error("init Channel:", e);
			connection = null;
			instance = null;
			instance = null;
		}
	}
	
	private MQSender() {
		
	}
	
	public static MQSender getInstance() {
		if(instance == null) {
			instance = init();
		}
		return instance;
	}
	
	private static synchronized MQSender init() {
		if(instance == null) {
			instance = new MQSender();
		}
		return instance;
	}
	
	
	/**
	 * 发送消息
	 * @param msg
	 */
	public void sendMsg(String msg) {
		try {
			if(channel == null || connection == null) {
				close();
				reInitChannel();
			}
			channel.basicPublish("", APNS_QUEUE_NAME, null, msg.getBytes("UTF-8"));
		} catch (Exception e) {
			close();
			log.error("sendMsg error cause:", e);
		}
	}
	
	/**
	 * 发送消息
	 * @param msg
	 */
	public void sendMsg(byte[] msg) {
		try {
			if(channel == null || connection == null) {
				close();
				reInitChannel();
			}
			channel.basicPublish("", APNS_QUEUE_NAME, null, msg);
		} catch (Exception e) {
			close();
			log.error("[sendMsg] error cause:", e);
		}
	}
	
	/**
	 * 关闭
	 * @param msg
	 */
	public static void close() {
		try {
			if(channel != null) {
				channel.close();
			}
			if(connection != null) {
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
			channel.queueDeclare(APNS_QUEUE_NAME, true, false, false, null);
		} catch (Exception e) {
			log.error("init Channel:", e);
		}
	}
}
