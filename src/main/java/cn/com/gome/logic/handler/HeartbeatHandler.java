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

/**
 * 心跳处理
 */
@HandlerContract(cmd = Command.CMD_HEARTBEAT)
public class HeartbeatHandler implements IMsgHandler {
	private static final Logger log = LoggerFactory.getLogger(HeartbeatHandler.class);

	@Override
	public void process(String clientKey, ProtocolPackage msg) {
		log.info("[process] clientKey=[{}],apppId=[{}],uid=[{}],clientId=[{}]", clientKey, msg.getAppId().trim(), msg.getUid(),
				msg.getClientId());
		msg.setAck(Constant.PACK_ACK.YES.value);
		TcpDataSendUtils.sendTcpData(clientKey, msg);
	}

	@Override
	public void process(MQMsg msg) {
		// TODO Auto-generated method stub
	}
}
