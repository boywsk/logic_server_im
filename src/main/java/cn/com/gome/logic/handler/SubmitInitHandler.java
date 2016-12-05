package cn.com.gome.logic.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.annotation.HandlerContract;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.service.GroupMsgService;
import cn.com.gome.logic.utils.TcpDataSendUtils;

/**
 * init seqid上报
 */
@HandlerContract(cmd = Command.CMD_SUBMIT_INIT_SEQ)
public class SubmitInitHandler implements IMsgHandler {
	Logger log = LoggerFactory.getLogger(SubmitInitHandler.class);
	
	@Override
	public void process(String clientKey, ProtocolPackage msg) {
		log.info("[process] cmd=[{}]", msg.getCommand());
		long uid = msg.getUid();
		// 修改数据库
		GroupMsgService service = new GroupMsgService();
		service.upateInitSeq(clientKey, msg);
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
