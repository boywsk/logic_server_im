package cn.com.gome.logic.handler;

import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.protobuf.ProtocolPackage;

/**
 * 消息处理器
 */
public interface IMsgHandler {
	
	/**
	 * 消息处理
	 * @param channel
	 * @param msg
	 */
	public void process(String clientKey, ProtocolPackage msg);
	
	
	/**
	 * MQ消息处理
	 * @param msg
	 */
	public void process(MQMsg msg);
}
