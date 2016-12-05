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
 * 用户登录；通过uid和token认证
 */
//@HandlerContract(cmd = Command.CMD_USER_LOGIN)
public class LoginHandler implements IMsgHandler {

	private static final Logger log = LoggerFactory.getLogger(LoginHandler.class);

	public void process(String clientKey, ProtocolPackage msg) {
		log.info("[process] uid=[{}],traceId=[{}],cmd=[{}]", msg.getTraceId(), msg.getUid(), msg.getCommand());
		long uid = msg.getUid();
		byte clientId = msg.getClientId();
		// 验证数据并生成返回结果
		ProtocolPackage pack = generatePack(clientKey, msg);
		pack.setAck(Constant.PACK_ACK.YES.value);
		// 发送消息
		pack.setCommand(Command.CMD_USER_LOGIN);
		pack.setReceiveId(uid);
		pack.setClientId(clientId);
		pack.setProtoBody(msg.getProtoBody());
//		pack.setRspIP(0L);
//		pack.setRspPort(0);
		TcpDataSendUtils.sendTcpData(clientKey, pack);
		log.info("[process] uid=[{}],traceId=[{}],clientId=[{}],cmd=[{}]", msg.getTraceId(), msg.getUid(), clientId, msg.getCommand());
	}

	/**
	 * 验证数据并生成返回结果
	 * 
	 * @param msg
	 * @return
	 */
	private ProtocolPackage generatePack(String clientKey, ProtocolPackage msg) {
		log.info("[generatePack] traceId=[{}],uid=[{}],cmd=[{}]", msg.getTraceId(), msg.getUid(), msg.getCommand());
		ProtocolPackage pack = msg;
		try {
			UserService service = new UserService();
			int result = service.userLogin(clientKey, msg);
			pack.setResult((byte) result);
		} catch (Exception e) {
			log.error("[generatePack] traceId=[{}],uid=[{}]", msg.getTraceId(), msg.getUid());
			log.error("[generatePack] error:", e);
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
