package cn.com.gome.logic.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.annotation.HandlerContract;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.service.NoticeMsgService;
import cn.com.gome.logic.utils.TcpDataSendUtils;

/**
 * 功能消息客户端收到后回执
 */
@HandlerContract(cmd = Command.CMD_NOTICE_ACK)
public class NoticeAckHandler implements IMsgHandler {
	Logger log = LoggerFactory.getLogger(NoticeAckHandler.class);
	@Override
	public void process(String clientKey, ProtocolPackage msg) {
		String appId = msg.getAppId();
		long uid = msg.getUid();
		long tranceId = msg.getTraceId();		
		log.info("[noticeAck] tranceId=[{}],appId=[{}],uid=[{}]", tranceId, appId, uid);
		NoticeMsgService service = new NoticeMsgService();
		service.noticeAck(msg);
		// 发送消息
		msg.setAck(Constant.PACK_ACK.YES.value);
		msg.setReceiveId(uid);
		msg.setResult((byte) 0);
		msg.setProtoBody(new byte[0]);
		TcpDataSendUtils.sendTcpData(clientKey, msg);
	}

	@Override
	public void process(MQMsg msg) {
		// TODO Auto-generated method stub

	}

}
