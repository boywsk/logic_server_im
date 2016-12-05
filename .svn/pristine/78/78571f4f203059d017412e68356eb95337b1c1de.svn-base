package cn.com.gome.logic.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.annotation.HandlerContract;
import cn.com.gome.logic.handler.IMsgHandler;
import cn.com.gome.logic.utils.TcpDataSendUtils;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.service.GroupMsgService;

/**
 * 聊天
 */
@HandlerContract(cmd = Command.CMD_IM_SEND_MSG)
public class IMMsgSendHandler implements IMsgHandler {

	private static final Logger log = LoggerFactory.getLogger(IMMsgSendHandler.class);

	public void process(String clientKey, ProtocolPackage msg) {
		log.info("[process] appId=[{}],senderId=[{}],tranceId=[{}]",msg.getAppId(), msg.getUid(), msg.getTraceId());
		long uid = msg.getUid();
		byte clientId = msg.getClientId();
		ProtocolPackage pack = generatePack(clientKey, msg);
		pack.setAck(Constant.PACK_ACK.YES.value);
		// 返回消息
		pack.setReceiveId(uid);
		pack.setClientId(clientId);
//		pack.setRspIP(0L);
//		pack.setRspPort(0);
		TcpDataSendUtils.sendTcpData(clientKey, pack);
	}

	/**
	 * 保存聊天消息；并转发
	 * 
	 * @param str
	 * @param msg
	 * @return
	 */
	private ProtocolPackage generatePack(String clientKey, ProtocolPackage msg) {
		log.info("[generatePack] senderId=[{}],tranceId=[{}]", msg.getUid(), msg.getTraceId());
		GroupMsgService service = new GroupMsgService();
		ProtocolPackage pack = service.saveAndForwardMsg(msg);
		return pack;
	}

	/**
	 * 发送推过api上来的消息
	 */
	public void process(MQMsg mqMsg) {
		log.info("[process mq] api send message start...");
		GroupMsgService service = new GroupMsgService();
		service.saveAndForwardApiMsg(mqMsg);
	}
}
