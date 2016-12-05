package cn.com.gome.logic.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import cn.com.gome.logic.cache.ServerCache;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.handler.IMsgHandler;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.utils.ProtocolPackageUtils;

/**
 * MQ资讯类消息(客服)消费者
 */
public class ConsultMQRecver {
	private static Logger log = LoggerFactory.getLogger(ConsultMQRecver.class);
	private static ConsultMQRecver instance = null;
	
	private ConsultMQRecver() {
	}

	public synchronized static ConsultMQRecver getInstance() {
		if (instance == null) {
			instance = new ConsultMQRecver();
		}
		return instance;
	}
	
	public void init() throws Exception {
		log.info("[ConsultMQRecver init] start...");
		Connection connection = null;
		Channel channel = null;
		try {
			String host = Global.MQ_HOST;
			int port = Global.MQ_PORT;
			String virtualHost = Global.MQ_VIRTUALHOST;
			String username = Global.MQ_USERNAME;
			String password = Global.MQ_PASSWORD;
			String queueName = Global.CUSTOMER_PRODUCER;
			Object[] params = { host, port, virtualHost, username, password, queueName };
			log.info("[ConsultMQRecver init]  MQRecver host=[{}],port=[{}],virtualHost=[{}],username=[{}],password=[{}],queueName=[{}]",
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
				byte[] message = delivery.getBody();
				log.info("[init while] received msg=[{}]", message);
				ProtocolPackage pack = ProtocolPackageUtils.byteArray2Package(message);
				dispatcher(pack);
			}
		} catch (Exception e) {
			log.error("[ConsultMQRecver init while] cause is:", e);
			log.error("[ConsultMQRecver init while] cause is:", e);
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
	private void dispatcher(ProtocolPackage pack) {
		try {
			MQMsg mqMsg = new MQMsg();
			mqMsg.setPack(pack);
			short cmd = pack.getCommand();
			IMsgHandler handler = ServerCache.getBaseHandler(cmd);
			if (null != handler) {
				handler.process(mqMsg);
			} else {
				log.info("[dispatcher] handler is null,so drop message;cmd=[[]]", cmd);
			}
		} catch (Exception e) {
			log.error("[dispatcher] cause is:", e);
		}
	}
}
