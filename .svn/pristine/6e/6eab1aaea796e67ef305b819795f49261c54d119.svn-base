package cn.com.gome.logic.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.annotation.HandlerContract;
import cn.com.gome.logic.handler.IMsgHandler;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.utils.TcpDataSendUtils;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.service.GroupMsgService;

/**
 * 处理客户端提交上来的readSeq
 */
@HandlerContract(cmd = Command.CMD_SUBMIT_READ_SEQ)
public class SubmitReadHandler implements IMsgHandler {
	Logger log = LoggerFactory.getLogger(SubmitReadHandler.class);

	@Override
	public void process(String clientKey, ProtocolPackage msg) {
		int tranceId = msg.getTraceId();
		log.info("[process] tranceId=[{}],cmd=[{}]", tranceId, msg.getCommand());
		long uid = msg.getUid();
		// 修改数据库
		GroupMsgService service = new GroupMsgService();
		service.upateReadSeq(clientKey, msg);
		// 发送消息
		msg.setAck(Constant.PACK_ACK.YES.value);
		msg.setReceiveId(uid);
		msg.setResult((byte)0);
		msg.setProtoBody(new byte[0]);
//		msg.setRspIP(0L);
//		msg.setRspPort(0);
		TcpDataSendUtils.sendTcpData(clientKey, msg);
	}

	@Override
	public void process(MQMsg msg) {
		// TODO Auto-generated method stub
	}

}
