package cn.com.gome.logic.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.annotation.HandlerContract;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.protobuf.ProtoIM.GetGroupByIdMsg;
import cn.com.gome.logic.protobuf.ProtoIM.UserData;
import cn.com.gome.logic.service.GroupService;
import cn.com.gome.logic.utils.TcpDataSendUtils;
import cn.com.gome.logic.protobuf.ProtocolPackage;

/**
 * 根据群组id获取群组消息
 */
@HandlerContract(cmd = Command.CMD_GROUP_BY_ID)
public class GetGroupByIdHandler implements IMsgHandler {
	Logger log = LoggerFactory.getLogger(GetGroupByIdHandler.class);
	
	@Override
	public void process(String clientKey, ProtocolPackage msg) {
		log.info("[process] cmd=[{}]", msg.getCommand());
		long uid = msg.getUid();
		ProtocolPackage pack = generatePack(msg);
		pack.setAck(Constant.PACK_ACK.YES.value);
		// 返回消息
		pack.setReceiveId(uid);
		TcpDataSendUtils.sendTcpData(clientKey, pack);
	}
	
	private ProtocolPackage generatePack(ProtocolPackage msg) {
		ProtocolPackage pack = msg;
		String appId = msg.getAppId().trim();
		GroupService service = new GroupService();
		byte[] body = msg.getProtoBody();
		log.info("[generatePack] appId=[{}],uid=[{}],body size=[{}]", appId, msg.getUid(), body.length);
		try {
			GetGroupByIdMsg pbMsg = GetGroupByIdMsg.parseFrom(body);
			long uid = pbMsg.getUid();
			String groupId = pbMsg.getGroupId();
			UserData list = service.getGrpupById(appId, uid, groupId);
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
