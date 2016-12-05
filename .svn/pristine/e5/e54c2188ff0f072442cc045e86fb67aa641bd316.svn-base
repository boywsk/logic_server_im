package cn.com.gome.logic.handler;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.annotation.HandlerContract;
import cn.com.gome.logic.handler.IMsgHandler;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.utils.TcpDataSendUtils;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.protobuf.ProtoIM.ListOffileMsg;
import cn.com.gome.logic.protobuf.ProtoIM.UserData;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.service.GroupMsgService;

/**
 * 聊天消息获取
 */
@HandlerContract(cmd = Command.CMD_IM_OFFLINE_MSG)
public class ListMsgHandler implements IMsgHandler {
	private static final Logger log = LoggerFactory.getLogger(ListMsgHandler.class);

	public void process(String clientKey, ProtocolPackage msg) {
		String appId = msg.getAppId().trim();
		log.info("[process] appId=[{}],uid=[{}]", appId, msg.getUid());
		generatePack(clientKey, msg);
	}

	public void generatePack(String clientKey, ProtocolPackage msg) {
		long uid = msg.getUid();
		log.info("[generatePack] uid=[{}]", uid);
//		ProtocolPackage pack = msg.clone();
		GroupMsgService service = new GroupMsgService();
		try {
			byte[] body = msg.getProtoBody();
			ListOffileMsg offlineMsg = ListOffileMsg.parseFrom(body);
			String appId = msg.getAppId().trim();
			String groupId = offlineMsg.getGroupId();
			long seqId = offlineMsg.getMsgSeqId();
			int size = offlineMsg.getSize();
			List<UserData> dataList = service.listMsg(appId, groupId, uid, seqId, size);
			log.info("[generatePack] appId=[{}],uid=[{}],dataList size=[{}]", appId, msg.getUid(), dataList.size());
			for(UserData data : dataList) {
//				ProtocolPackage pack = new ProtocolPackage();
//				pack.setHead(msg.getHead());
				//测试日志
//				List<ImMsg> imMsgs = data.getMsgList();
//				for(ImMsg imMsg : imMsgs) {
//					String msgId = imMsg.getMsgId();
//					long seqId2 = imMsg.getMsgSeqId();
//					log.info("[generatePack] appId=[{}],uid=[{}],msgId=[{}],seqId=[{}]", appId, msg.getUid(), msgId, seqId2);
//				}
				ProtocolPackage pack = msg.deepCopy();
				pack.setProtoBody(data.toByteArray());
				pack.setReceiveId(uid);
				pack.setAck(Constant.PACK_ACK.YES.value);
				// 返回消息
				pack.setReceiveId(uid);
				TcpDataSendUtils.sendTcpData(clientKey, pack);
			}
		} catch (Exception e) {
//			ProtocolPackage pack = new ProtocolPackage();
//			pack.setHead(msg.getHead());
			ProtocolPackage pack = msg.deepCopy();
			log.error("[generatePack] uid is:", e);
			pack.setProtoBody(new byte[0]);
			pack.setReceiveId(uid);
			pack.setAck(Constant.PACK_ACK.YES.value);
			pack.setResult((byte) -1);
			TcpDataSendUtils.sendTcpData(clientKey, pack);
		}
	}

	@Override
	public void process(MQMsg msg) {
		// TODO Auto-generated method stub
	}
}
