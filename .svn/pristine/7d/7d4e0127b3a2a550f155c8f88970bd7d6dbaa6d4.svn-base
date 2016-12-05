package cn.com.gome.logic.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.handler.IMsgHandler;
import cn.com.gome.logic.cache.ServerCache;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.model.MQMsg;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * rabbitmq消息接受者
 */
public class MQRecver {
	private static Logger log = LoggerFactory.getLogger(MQRecver.class);

	private static MQRecver instance = null;

	private MQRecver() {
	}

	public synchronized static MQRecver getInstance() {
		if (instance == null) {
			instance = new MQRecver();
		}
		return instance;
	}

	public void init() throws Exception {
		log.info("[init] start...");
		Connection connection = null;
		Channel channel = null;
		try {
			String host = Global.MQ_HOST;
			int port = Global.MQ_PORT;
			String virtualHost = Global.MQ_VIRTUALHOST;
			String username = Global.MQ_USERNAME;
			String password = Global.MQ_PASSWORD;
			String queueName = Global.MQ_QUEUENAME;
			Object[] params = { host, port, virtualHost, username, password, queueName };
			log.info("[init]  MQRecver host=[{}],port=[{}],virtualHost=[{}],username=[{}],password=[{}],queueName=[{}]",
					params);
			ConnectionFactory factory = new ConnectionFactory();
			factory.setUsername(username);
			factory.setPassword(password);
			factory.setVirtualHost(virtualHost);
			factory.setHost(host);
			factory.setPort(port);
			factory.setAutomaticRecoveryEnabled(true);
			factory.setConnectionTimeout(5000);
			factory.setShutdownTimeout(3000);
			factory.setRequestedHeartbeat(5);
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.queueDeclare(queueName, true, false, false, null);
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, true, consumer);
			while (true) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				String message = new String(delivery.getBody(), "UTF-8");
				log.info("[init while] received msg=[{}]", message);
				dispatcher(message);
			}
		} catch (Exception e) {
			log.error("[init while] cause is:", e);
			log.error("[ApnsMQRecver init while] cause is:", e);
			if (channel != null) {
				channel.close();
			}
			if (connection != null) {
				connection.close();
			}
			init();
			return;
		}
	}

	/*
	 * 发送消息到逻辑服务器
	 */
	private void dispatcher(String message) {
		try {
			MQMsg msMsg = JSON.parseObject(message, MQMsg.class);
			short cmd = msMsg.getCmd();
			IMsgHandler handler = ServerCache.getBaseHandler(cmd);
			if (null != handler) {
				handler.process(msMsg);
			} else {
				log.info("[dispatcher] handler is null,so drop message;cmd=[[]]", cmd);
			}
		} catch (Exception e) {
			log.error("[dispatcher] cause is:", e);
		}
	}
}
