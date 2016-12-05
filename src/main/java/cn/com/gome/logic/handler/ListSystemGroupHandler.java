package cn.com.gome.logic.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.annotation.HandlerContract;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.protobuf.ProtoIM.ListGroupMsg;
import cn.com.gome.logic.protobuf.ProtoIM.UserData;
import cn.com.gome.logic.service.GroupService;
import cn.com.gome.logic.utils.TcpDataSendUtils;

/**
 * 获取系统群组列表
 */
@HandlerContract(cmd = Command.CMD_LIST_SYS_GROUP)
public class ListSystemGroupHandler implements IMsgHandler {
	Logger log = LoggerFactory.getLogger(ListSystemGroupHandler.class);
	
	public void process(String str, ProtocolPackage msg) {
		log.info("[process] uid=[{}]", msg.getUid());
		long uid = msg.getUid();
		ProtocolPackage pack = generatePack(msg);
		pack.setAck(Constant.PACK_ACK.YES.value);
		// 返回消息
		pack.setReceiveId(uid);
		TcpDataSendUtils.sendTcpData(str, pack);
	}

	private ProtocolPackage generatePack(ProtocolPackage msg) {
		ProtocolPackage pack = msg;
		String appId = msg.getAppId().trim();
		GroupService service = new GroupService();
		byte[] body = msg.getProtoBody();
		log.info("[generatePack] appId=[{}],uid=[{}],body size=[{}]", appId, msg.getUid(), body.length);
		try {
			ListGroupMsg pbMsg = ListGroupMsg.parseFrom(body);
			long uid = pbMsg.getUid();
			
			long time = pbMsg.getTime();
			UserData list = service.listSystemGroupByUid(appId, uid, time);
			pack.setProtoBody(list.toByteArray());
		} catch (Exception e) {
			log.error("[generatePack] appId=[{}],uid=[{}]", appId, msg.getUid());
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
