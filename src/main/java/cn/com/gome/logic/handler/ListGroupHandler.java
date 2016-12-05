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
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.protobuf.ProtoIM.ImGroup;
import cn.com.gome.logic.protobuf.ProtoIM.ListGroupMsg;
import cn.com.gome.logic.protobuf.ProtoIM.UserData;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.service.GroupService;

/**
 * 获取群组列表
 */
@HandlerContract(cmd = Command.CMD_LIST_GROUP)
public class ListGroupHandler implements IMsgHandler {
	private static final Logger log = LoggerFactory.getLogger(ListGroupHandler.class);
	public void process(String clientKey, ProtocolPackage msg) {
		log.info("[process] uid=[{}]", msg.getUid());
//		long uid = msg.getUid();
		generatePack(clientKey, msg);
//		ProtocolPackage pack = generatePack(clientKey, msg);
//		pack.setAck(Constant.PACK_ACK.YES.value);
//		pack.setReceiveId(uid);
//		TcpDataSendUtils.sendTcpData(clientKey, pack);
	}

	private void generatePack(String clientKey, ProtocolPackage msg) {
//		ProtocolPackage pack = msg;
		String appId = msg.getAppId().trim();
		GroupService service = new GroupService();
		byte[] body = msg.getProtoBody();
		long uid = msg.getUid();
		log.info("[generatePack] appId=[{}],uid=[{}],body size=[{}]", appId, uid, body.length);
		try {
			ListGroupMsg pbMsg = ListGroupMsg.parseFrom(body);
			long time = pbMsg.getTime();
			UserData data = service.listGroupByUid(appId, uid, time);
			int dataSize = data.getSerializedSize();
			log.info("[generatePack] uid=[{}], UserData size=[{}]", uid, dataSize);
			if(dataSize > Global.MAX_PACK_SIZE) {
				UserData.Builder pbGroupList = UserData.newBuilder();
				List<ImGroup> list = data.getGroupList();
				log.info("[generatePack] appId=[{}],uid=[{}],pbGroupList size=[{}]", appId, msg.getUid(), list.size());
				for(ImGroup imGroup : list) {
					ProtocolPackage pack = msg.deepCopy();
					int size = pbGroupList.build().getSerializedSize();
					int imGroupSize = imGroup.getSerializedSize();
					int totalSize = size + imGroupSize;
					log.info("[generatePack] uid=[{}], imGroupSize=[{}],totalSize=[{}]", uid, imGroupSize, totalSize);
					if(imGroupSize > Global.MAX_PACK_SIZE) {
						log.error("[generatePack] imGroup is lage!!!!!!!!!!!!!!!!!!!,imGroupSize=[{}]", imGroupSize);
						continue;
					}
					if(totalSize > Global.MAX_PACK_SIZE) {
						UserData userData = pbGroupList.build();
						pack.setProtoBody(userData.toByteArray());
						pack.setAck(Constant.PACK_ACK.YES.value);
						pack.setReceiveId(uid);
						TcpDataSendUtils.sendTcpData(clientKey, pack);
						pbGroupList = UserData.newBuilder();
					}
					
//					if((size >= Global.MIN_PACK_SIZE && size <= Global.MAX_PACK_SIZE) ||
//							(totalSize >= Global.MIN_PACK_SIZE && totalSize <= Global.MAX_PACK_SIZE)) {
//						UserData userData = pbGroupList.build();
//						pack.setProtoBody(userData.toByteArray());
//						pack.setAck(Constant.PACK_ACK.YES.value);
//						pack.setReceiveId(uid);
//						TcpDataSendUtils.sendTcpData(clientKey, pack);
//						pbGroupList = UserData.newBuilder();
//					}
					pbGroupList.addGroup(imGroup);
				}
				ProtocolPackage pack = msg.deepCopy();
				UserData userData = pbGroupList.build();
				pack.setProtoBody(userData.toByteArray());
				pack.setAck(Constant.PACK_ACK.YES.value);
				pack.setReceiveId(uid);
				TcpDataSendUtils.sendTcpData(clientKey, pack);
			} else {
				ProtocolPackage pack = msg.deepCopy();
				pack.setProtoBody(data.toByteArray());
				pack.setAck(Constant.PACK_ACK.YES.value);
				pack.setReceiveId(uid);
				TcpDataSendUtils.sendTcpData(clientKey, pack);
			}
		} catch (Exception e) {
			ProtocolPackage pack = msg.deepCopy();
			log.error("[generatePack] appId=[{}],uid=[{}]", appId, msg.getUid());
			log.error("[generatePack] error is:", e);
			pack.setResult((byte) -1);
			pack.setProtoBody(new byte[0]);
			pack.setReceiveId(uid);
			TcpDataSendUtils.sendTcpData(clientKey, pack);
		}
		
		//return pack;
	}

	@Override
	public void process(MQMsg msg) {
		// TODO Auto-generated method stub
	}
}
