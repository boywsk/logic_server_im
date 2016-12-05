package cn.com.gome.logic.handler;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.protobuf.InvalidProtocolBufferException;
import cn.com.gome.logic.annotation.HandlerContract;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.protobuf.ProtoIM.UserData;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.service.NoticeMsgService;
import cn.com.gome.logic.utils.TcpDataSendUtils;

/**
 * 获取 功能性离线消息
 * @author wanghuanfei
 */
@HandlerContract(cmd = Command.CMS_OFFLINE_NOTICE_MSG)
public class ListNoticeHandler implements IMsgHandler {
	Logger log = LoggerFactory.getLogger(ListNoticeHandler.class);
	@Override
	public void process(String clientKey, ProtocolPackage msg) {

		String appId = msg.getAppId();
		long uid = msg.getUid();
		long tranceId = msg.getTraceId();
		log.info("[ListNoticeHandler] tranceId=[{}],appId=[{}],uid=[{}]", tranceId, appId, uid);
		List<ProtocolPackage> packs = generatePack(msg);
		int size = packs.size();
		for(int i = 0; i < size; i ++) {
			ProtocolPackage pack = packs.get(i);
			pack.setAck(Constant.PACK_ACK.YES.value);
			/*if(i == size - 1) {
				pack.setLastPack(Constant.LAST_PACK.YES.value);
			}*/
			try {
				UserData data = UserData.parseFrom(pack.getProtoBody());
				int count = data.getNoticeMsgCount();
				log.info("[generatePack] appId=[{}],uid=[{}],NoticeMsg count=[{}]", appId, msg.getUid(), count);
			} catch (InvalidProtocolBufferException e) {
				log.error("[process]:", e);
			}
			// 发送消息
			pack.setReceiveId(uid);
			TcpDataSendUtils.sendTcpData(clientKey, pack);
		}
	}
	
	public List<ProtocolPackage> generatePack(ProtocolPackage msg) {
		List<ProtocolPackage> list = new ArrayList<ProtocolPackage>();
		log.info("[process] uid=[{}]", msg.getUid());
		ProtocolPackage pack = msg;
		NoticeMsgService service = new NoticeMsgService();
		try {
			String appId = msg.getAppId();
			List<UserData> dataList = service.listNoitceMsg(msg);
			log.info("[generatePack] appId=[{}],uid=[{}],dataList size=[{}]", appId, msg.getUid(), dataList.size());
			for(UserData data : dataList) {
				int count = data.getNoticeMsgCount();
				log.info("[generatePack] appId=[{}],uid=[{}],NoticeMsg count=[{}]", appId, msg.getUid(), count);
				pack.setProtoBody(data.toByteArray());
				list.add(pack);
			}
		} catch (Exception e) {
			log.error("[generatePack]:", e);
			pack.setResult((byte) -1);
			pack.setProtoBody(new byte[0]);
		}
		
		return list;
	}

	@Override
	public void process(MQMsg msg) {
		// TODO Auto-generated method stub

	}

}
