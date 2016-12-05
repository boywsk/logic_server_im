package cn.com.gome.logic.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import cn.com.gome.logic.apns.ApnsPushManager;
import cn.com.gome.logic.global.Global;

/**
 * rabbitmq apns消息接受者
 */
public class ApnsMQRecver {
private static Logger log = LoggerFactory.getLogger(ApnsMQRecver.class);
	
	private static ApnsMQRecver instance = null;
	
	private ApnsMQRecver() {}
	
	public synchronized static ApnsMQRecver getInstance() {
		if(instance == null) {
			instance = new ApnsMQRecver();
		}
		return instance;
	}
	
	public void init() throws Exception {
		log.info("[ApnsMQRecver init]...");
		String host = Global.MQ_HOST;
		int port = Global.MQ_PORT;
		String virtualHost = Global.MQ_VIRTUALHOST;
		String username = Global.MQ_USERNAME;
		String password = Global.MQ_PASSWORD;
		String queueName = Global.APNS_QUEUENAME;
		Object[] params = {host, port, virtualHost, username, password, queueName}; 
		log.info("[ApnsMQRecver init] host=[{}], port=[{}],virtualHost=[{}],username=[{}],password=[{}],queueName=[{}]", params);
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername(username);
		factory.setPassword(password);
		factory.setVirtualHost(virtualHost);
		factory.setHost(host);
		factory.setPort(port);
		factory.setAutomaticRecoveryEnabled(true);
		factory.setConnectionTimeout(5000);
		factory.setRequestedHeartbeat(5);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		//channel.exchangeDeclare("im-exchanges", "direct");
		channel.queueDeclare(queueName, true, false, false, null);
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, true, consumer);

		while (true) {
			try {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				dispatcher(delivery.getBody());
			} catch (Exception e) {
				log.error("[ApnsMQRecver init while] cause is:", e);
				init();
			}
		}
	}
	
	private void dispatcher(byte[] data) {
		try {
			ApnsPushManager pusher = new ApnsPushManager();
			pusher.push(data);
		} catch (Exception e) {
			log.error("[dispatcher] cause is:", e);
		}
		
	}
}
