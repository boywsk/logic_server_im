package cn.com.gome.logic.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.handler.IMsgHandler;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.service.UserService;
import cn.com.gome.logic.utils.TcpDataSendUtils;

/**
 * 清除push计数
 */
//@HandlerContract(cmd = Command.CMD_CLEAN_PUSH_COUNT)
public class CleanPushCountHandler implements IMsgHandler {
	Logger log = LoggerFactory.getLogger(CleanPushCountHandler.class);

	@Override
	public void process(String clientKey, ProtocolPackage msg) {
		log.info("[process] uid=[{}],cmd=[{}]", msg.getUid(), msg.getCommand());
		UserService service = new UserService();
		msg.setAck(Constant.PACK_ACK.YES.value);
		service.cleanPushCount(clientKey, msg);
		
		msg.setAck(Constant.PACK_ACK.YES.value);
		msg.setReceiveId(msg.getUid());
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
