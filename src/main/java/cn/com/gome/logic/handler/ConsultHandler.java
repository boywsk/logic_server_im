package cn.com.gome.logic.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.annotation.HandlerContract;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.mq.ConsultMQSender;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.service.GroupMsgService;
import cn.com.gome.logic.utils.ProtocolPackageUtils;
import cn.com.gome.logic.utils.TcpDataSendUtils;

/**
 * 咨询类聊天信息(客服类等)
 */
@HandlerContract(cmd = Command.CMD_CONSULT_IM_MSG)
public class ConsultHandler implements IMsgHandler {
	private static final Logger log = LoggerFactory.getLogger(ConsultHandler.class);
	
	@Override
	public void process(String clientKey, ProtocolPackage msg) {
		log.info("[process] appId=[{}],senderId=[{}]", msg.getUid(), msg.getAppId());
		long uid = msg.getUid();
		byte clientId = msg.getClientId();
		ProtocolPackage pack = generatePack(clientKey, msg);
		pack.setAck(Constant.PACK_ACK.YES.value);
		// 返回消息
		pack.setReceiveId(uid);
		pack.setClientId(clientId);
//		pack.setRspIP(msg.getRspIP());
//		pack.setRspPort(msg.getRspPort());
		TcpDataSendUtils.sendTcpData(clientKey, pack);
		log.info("[process end] appId=[{}],senderId=[{}]", msg.getUid(), msg.getAppId());
	}

	/**
	 * 保存聊天消息；并转发
	 * 
	 * @param str
	 * @param msg
	 * @return
	 */
	private ProtocolPackage generatePack(String clientKey, ProtocolPackage msg) {
		log.info("[generatePack] senderId=[{}]", msg.getUid());
		GroupMsgService service = new GroupMsgService();
		ProtocolPackage pack = service.consultMsg(msg);
		return pack;
	}
	
	@Override
	public void process(MQMsg mqMsg) {
		ProtocolPackage msg = mqMsg.getPack();
		log.info("[process] appId=[{}],senderId=[{}]", msg.getAppId(), msg.getUid());
		long uid = msg.getUid();
		byte clientId = msg.getClientId();
		ProtocolPackage pack = this.generatePack("", msg);
		pack.setAck(Constant.PACK_ACK.YES.value);
		// 返回消息
		pack.setReceiveId(uid);
		pack.setClientId(clientId);
		byte[] data = ProtocolPackageUtils.package2Byte(pack);
		ConsultMQSender.getInstance().sendMsg(data, uid);
		log.info("[process] 客服消息，回包成功（转发包）！ appId=[{}],senderId=[{}]", msg.getAppId(), uid);
	}
}