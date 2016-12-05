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
import cn.com.gome.logic.service.UserService;

/**
 * 用户登出
 */
//@HandlerContract(cmd = Command.CMD_USER_LOGOUT)
public class LogoutHandler implements IMsgHandler {
	private static final Logger log = LoggerFactory.getLogger(LogoutHandler.class);

	public void process(String clientKey, ProtocolPackage msg) {
		log.info("[process] uid=[{}]", msg.getUid());
		long uid = msg.getUid();
		// 验证数据并生成返回结果
		ProtocolPackage pack = generatePack(clientKey, msg);
		pack.setAck(Constant.PACK_ACK.YES.value);
		// 发送消息
		pack.setProtoBody(new byte[0]);
		pack.setReceiveId(uid);
		TcpDataSendUtils.sendTcpData(clientKey, pack);
	}

	private ProtocolPackage generatePack(String clientKey, ProtocolPackage msg) {
		log.info("[generatePack] uid=[{}]", msg.getUid());
		ProtocolPackage pack = msg;
		try {
			UserService service = new UserService();
			service.userLogout(clientKey, msg);
			pack.setResult((byte) 0);
		} catch (Exception e) {
			log.error("[generatePack] error is:", e);
			pack.setResult((byte) -1);
			pack.setProtoBody(new byte[0]);
		}
		return pack;
	}

	@Override
	public void process(MQMsg msg) {
		// TODO Auto-generated method stub
	}
}
