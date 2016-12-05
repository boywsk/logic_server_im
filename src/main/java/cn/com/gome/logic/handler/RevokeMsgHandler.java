package cn.com.gome.logic.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.annotation.HandlerContract;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.service.RevokeMsgService;
import cn.com.gome.logic.utils.TcpDataSendUtils;

/**
 * 撤回消息相关操作
 */
@HandlerContract(cmd = Command.CMD_REVOKE_MSG)
public class RevokeMsgHandler implements IMsgHandler {
	Logger log = LoggerFactory.getLogger(RevokeMsgHandler.class);
	
	@Override
	public void process(String clientKey, ProtocolPackage msg) {
		int traceId = msg.getTraceId();
		long uid = msg.getUid();
		String appId = msg.getAppId();
		log.info("[RevokeMsgHandler] appId=[{}],tranceId=[{}],uid=[{}]", appId, traceId, uid);
		ProtocolPackage retMsg = new ProtocolPackage();
		RevokeMsgService service = new RevokeMsgService();
		int result = service.revokeAndForwardMsg(msg);
		retMsg.setHead(msg.getHead());
		retMsg.setAck(Constant.PACK_ACK.YES.value);
		retMsg.setReceiveId(uid);
		retMsg.setRspIP(msg.getRspIP());
		retMsg.setRspPort(msg.getRspPort());
		retMsg.setResult((byte)result);
		// 发送消息
		TcpDataSendUtils.sendTcpData(clientKey, retMsg);
	}

	@Override
	public void process(MQMsg msg) {
		// TODO Auto-generated method stub

	}

}
