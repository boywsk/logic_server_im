package cn.com.gome.logic.test.mq;

import com.rabbitmq.client.ConnectionFactory;

import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.model.GroupMsg;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.utils.StringUtils;

import com.rabbitmq.client.Connection;
import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;

public class MQSend {
//	private final static String QUEUE_NAME = "PUSH_TEST";
	
	private final static String QUEUE_NAME = "push-queue-sdk-devel";//PUSH_TEST queue_push

	public static void main(String[] argv) throws Exception {
		
		ConnectionFactory factory = new ConnectionFactory();
//		factory.setHost("192.168.130.42");//10.128.60.26 192.168.130.42
//		factory.setUsername("test");//admin test
//		factory.setPassword("test");//www.glodon.c0m test
//		factory.setVirtualHost("test_host");//queue_host test_host
		
		factory.setHost("10.125.3.11");//10.128.60.26 192.168.130.42
		factory.setPort(5672);
		factory.setUsername("gome");//admin test
		factory.setPassword("gome");//www.glodon.c0m test
		factory.setVirtualHost("push");//queue_host test_host
		
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(QUEUE_NAME, true, false, false, null);
		
		
//		String message = "{\"action\":1,\"sender\":{\"id\":\"123456\",\"name\":\"wangye\"},"
//				+ "\"actors\":[{\"id\":\"6666\",\"name\":\"wy\"}],\"argv\":{\"validateType\":1,"
//				+ "\"avatar\":\"http://192.168.130.43:8081/i/user/201504/498_20.png,http://192.168.130.43:8081/i/user/201504/498_120.png,"
//				+ "http://192.168.130.43:8081/i/user/201504/498_ori.png\"}}";
		
//		String message = "{\"action\":2,\"sender\":{\"id\":\"123456\",\"name\":\"wangye\"},"
//				+ "\"actors\":[{\"id\":\"123\",\"name\":\"wy\"}],\"argv\":{\"agreeType\":1}}";
//		
		String message = "{\"action\":3,\"sender\":{\"id\":\"5768623340797760196\",\"name\":\"黎鹏\"}"
				+ ",\"actors\":[{\"id\":\"5555\",\"name\":\"Mingren\"},{\"id\":\"5555\","
				+ "\"name\":\"你是谁的\"}],\"noticeUsers\":[],\"argv\":"
				+ "{\"avatar\":\"http://192.168.130.43:8081/i/user/201503/370_120.png\",\"groupDesc\":\"ss\","
				+ "\"groupName\":\"dddd\", \"groupid\":173}}";
		
		
//		String message = "{\"action\":3,\"sender\":{\"id\":\"5983876188250243241\",\"name\":\"武汉市q\"},"
//				+ "\"actors\":[{\"id\":\"5986012468731089107\",\"name\":\"这种\"},"
//				+ "{\"id\":\"5980600427320848871\",\"name\":\"在线hhhjj\"},{\"id\":\"5988503169247326370\","
//				+ "\"name\":\"我自?1¤7\"},{\"id\":\"5991398023221657712\",\"name\":\"?1¤7 ?1¤7\"}],\"noticeUsers\":[],\"argv\":{\"groupName\":\"这种、在线hhhjj、我自己、我 ?1¤7\",\"groupid\":318}}";
//		String createGroupMsg="{\"action\":3,\"sender\":{\"id\":\"5967926374714536667\",\"name\":\"龙猫\"},\"actors\":[{\"id\":\"5555\",\"name\":\"你是谁的\"}],\"noticeUsers\":[],\"argv\":{\"groupName\":\"你是谁的、飞飞\",\"groupid\":343}}";
//		String joinGroupMsg="{\"action\":4,\"sender\":{\"id\":\"5967926374714536667\",\"name\":\"龙猫\"},\"actors\":[{\"id\":\"5555\",\"name\":\"你是谁的\"}],\"noticeUsers\":[\"6666\"],\"argv\":{\"groupName\":\"你是谁的、飞飞\",\"groupid\":343}}";
//		String message = "{\"action\":4,\"sender\":{\"id\":\"5768623340797760196\",\"name\":\"黎鹏\"},"
//				+ "\"actors\":[{\"id\":\"5986012468731089107\",\"name\":\"这种\"}],"
//				+ "\"noticeUsers\":[\"5856221030582337699\",\"5981260420157477886\"],"
//				+ "\"argv\":{\"groupid\":172}}";
		
//		String message = "{\"action\":5,\"sender\":{\"id\":\"5768623340797760196\",\"name\":\"黎鹏\"},"
//				+ "\"actors\":[{\"id\":\"5986012468731089107\",\"name\":\"这种\"}],"
//				+ "\"noticeUsers\":[\"5768623340797760196\",\"5856221030582337699\",\"5981260420157477886\","
//				+ "\"5986012468731089107\"],\"argv\":{\"groupid\":172,\"quitType\":1}}";
		
//		String message = "{\"action\":6,\"sender\":{\"id\":\"5768623340797760196\",\"name\":\"黎鹏\"},"
//				+ "\"actors\":[],\"noticeUsers\":[\"5768623340797760196\",\"5856221030582337699\","
//				+ "\"5981260420157477886\",\"5986012468731089107\"],\"argv\":{\"groupid\":172}}";
		
//		String message = "{\"action\":7,\"sender\":{\"id\":\"5768623340797760196\",\"name\":\"黎鹏\"},"
//				+ "\"actors\":[],\"noticeUsers\":[\"5768623340797760196\",\"5856221030582337699\","
//				+ "\"5981260420157477886\",\"5986012468731089107\"],\"argv\":{\"groupid\":172}}";

//		String message = "{\"action\":8,\"sender\":{\"id\":\"5991476224940978727\",\"name\":\"洋溢\"},"
//				+ "\"actors\":[{\"id\":\"5991808950638273195\",\"name\":\"酸奶\"}],\"argv\":{}}";
		
		//channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
		
		//System.out.println(" [x] Sent '" + message + "'");
//		Thread.sleep(100000);
		
		MQMsg mqMsg = new MQMsg();
		mqMsg.setCmd(Command.CMD_IM_SEND_MSG);
		mqMsg.setAppId("123");
		mqMsg.setPersist(false);
		
		GroupMsg msg = new GroupMsg();
		msg.setMsgId(StringUtils.getUuid());
		msg.setGroupId("1_2");
		msg.setGroupName("test");
		msg.setGroupType(1);
		msg.setSenderId(1L);
		msg.setSenderName("name1");
		msg.setMsgBody("hello");
		mqMsg.setGroupMsg(msg);
		message = JSON.toJSONString(mqMsg);
		
		for (int i = 0; i < 1; i++) {
//			String message = "Hello World! i=" + i;
			channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
			System.out.println(" [x] Sent '" + message + "'");
			Thread.sleep(1000);
		}

		channel.close();
		connection.close();
	}
}
