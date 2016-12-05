package cn.com.gome.logic.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import cn.com.gome.logic.dao.NoticeMsgDao;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.model.notice.SaveNoticeMsg;
import cn.com.gome.logic.pbTools.NoticeMsgTools;
import cn.com.gome.logic.pool.ThreadPool;
import cn.com.gome.logic.protobuf.ProtoIM.NoticeAckMsg;
import cn.com.gome.logic.protobuf.ProtoIM.NoticeMsg;
import cn.com.gome.logic.protobuf.ProtoIM.UserData;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.utils.CheckClientTypeUtils;
import cn.com.gome.logic.worker.SendNoticeMsgWorker;

public class NoticeMsgService {
	private static Logger log = LoggerFactory.getLogger(NoticeMsgService.class);
	/**
	 * 分发NoticeMsg任务
	 */
	public void dispatchNoticeMsg(MQMsg msg, int traceId) {
		ThreadPool pool = ThreadPool.getInstance();
		String appId = msg.getAppId();
		short cmd = msg.getCmd();
		log.info("[dispatchNoticeMsg] step 2, traceId = [{}], appId = [{}], cdm = [{}]", traceId, appId, cmd);
		SendNoticeMsgWorker woker = new SendNoticeMsgWorker(msg.getNoticeMsgModel(), traceId, appId, cmd);
		pool.addTask(woker);
	}
	
	/**
	 * 删除功能性消息（逻辑删除）
	 * 逻辑删除，将平台信息置为0，下次则不会被拉取
	 * @param msg
	 */
	public void noticeAck(ProtocolPackage msg) {
		String appId = msg.getAppId();
		long uid = msg.getUid();
		int tranceId = msg.getTraceId();
		byte clientId = msg.getClientId();
		byte[] body = msg.getProtoBody();
		try {
			NoticeAckMsg noticeAck = NoticeAckMsg.parseFrom(body);
			String msgId = noticeAck.getMsgId();
			//long receiveSeqId = noticeAck.getReceiveSeqId();
			log.info("[noticeAck1] tranceId=[{}],appId=[{}],uid=[{}],clientId=[{}],msgId=[{}]",
					tranceId, appId, uid, clientId, msgId);
			//if(receiveSeqId <= 0) {
				NoticeMsgDao dao = new NoticeMsgDao();
				int fromPlatform = getFromPlatform(clientId, tranceId);// 确认收到消息平台
				if (fromPlatform > 0) {
					List<Integer> platforms = dao.getPlatform(appId, uid, msgId, tranceId);// 剩余未取消息平台
					for(int platform : platforms) {
						int newPlatform = platform ^ fromPlatform;// 剔除新收到的平台后，若为0，则全部确认收到，若不为0则更新。
						log.info(
								"[noticeAck2] tranceId=[{}],appId=[{}],uid=[{}],收到平台确认消息=[{}],未读平台信息=[{}],剩余平台信息=[{}]",
								tranceId, appId, uid, fromPlatform, platform, newPlatform);
//						if (newPlatform == 0) {
//							log.info("[noticeAck] [稳定后可删除日志] tranceId=[{}],删除功能性消息", tranceId);
//							dao.delMsg(appId, uid, groupId, msgId);
//						} else {
							dao.updatePlatform(appId, uid, msgId, newPlatform, tranceId);
//						}
					}
				} else {
					log.error("[noticeAck3] tranceId=[{}],No such fromPlatform=[{}]", tranceId, fromPlatform);
				}
			/*} else {
				String groupId = noticeAck.getGroupId();
				log.info("[noticeAck saveReceiveSeqId] tranceId=[{}],appId=[{}],groupId=[{}],uid=[{}],clientId=[{}]"
						+ ",msgId=[{}],receiveSeqId=[{}]", tranceId, appId, groupId, uid, clientId, msgId, receiveSeqId);
				GroupMemberDao memberDao = new GroupMemberDao();
				memberDao.saveReceiveSeqId(appId, uid, clientId, groupId, receiveSeqId);
			}*/
		} catch (InvalidProtocolBufferException e) {
			log.error("[noticeAck]:", e);
		}
	}
	
	/**
	 * 获取功能消息
	 * 
	 * @param msg
	 * @return
	 */
	public List<UserData> listNoitceMsg(ProtocolPackage msg) {
		List<UserData> dataList = new ArrayList<UserData>();
		UserData.Builder dataBuilder = UserData.newBuilder();
		String appId = msg.getAppId();
		long uid = msg.getUid();
		int traceId = msg.getTraceId();
		NoticeMsgDao dao = new NoticeMsgDao();
		log.info("[listNoitceMsg] tranceId=[{}],appId=[{}],uid=[{}]", traceId, appId, uid);
		List<SaveNoticeMsg> list = dao.listNotice(appId, uid, msg.getClientId(), traceId);
		if (list != null) {
			log.info("[listNoitceMsg] tranceId=[{}],appId=[{}],uid=[{}],list size=[{}]", traceId, appId,
					uid, list.size());
			for (SaveNoticeMsg saveNoticeMsg : list) {
				int pbMsgListLength = dataBuilder.build().getSerializedSize();
				NoticeMsg noticeMsg = NoticeMsgTools.notice2PbMsg(saveNoticeMsg, traceId);
				int pbSize = noticeMsg.getSerializedSize();
				int totalSize = pbMsgListLength + pbSize;
				log.info("[listNoitceMsg] appId=[{}],uid=[{}],pbMsgListLength=[{}],pbSize=[{}],totalSize=[{}]",
						appId, uid, pbMsgListLength, pbSize, totalSize);
				if(pbSize > Global.MAX_PACK_SIZE) {
					log.error("[listNoitceMsg] NoticeMsg is lage!!!!!!!!!!!!!!!!!!!,pbSize=[{}]", pbSize);
					continue;
				}
				
				if(totalSize > Global.MAX_PACK_SIZE) {
					dataList.add(dataBuilder.build());
					dataBuilder = UserData.newBuilder();
				}
				dataBuilder.addNoticeMsg(noticeMsg);
				
//				if((pbMsgListLength >= Global.MIN_PACK_SIZE && pbMsgListLength <= Global.MAX_PACK_SIZE) ||
//						(totalSize >= Global.MIN_PACK_SIZE && totalSize <= Global.MAX_PACK_SIZE)) {
//					dataList.add(dataBuilder.build());
//					dataBuilder = UserData.newBuilder();
//				}
//				dataBuilder.addNoticeMsg(noticeMsg);
				log.info("[listNoitceMsg] appId=[{}],uid=[{}],magId=[{}],",
						appId, uid, noticeMsg.getMsgId());
			}
			dataList.add(dataBuilder.build());
			log.info("[listNoitceMsg] tranceId=[{}],appId=[{}],uid=[{}],dataList size=[{}]", traceId, appId,
					uid, dataList.size());
		} else {
			log.info("[listNoitceMsg] tranceId=[{}],appId=[{}],uid=[{}],list is null!!!", traceId, appId,
					uid);
		}
		return dataList;
	}
	
	/**
	 * 获取平台信息
	 * 
	 * @param clientId
	 * @return
	 */
	private int getFromPlatform(int clientId, long tranceId) {
		int platform = 0;
		if (CheckClientTypeUtils.clientType_mobile(clientId)) {
			platform = 1;
		} else if (CheckClientTypeUtils.clientType_pc(clientId)) {
			platform = 2;
		} else if (clientId == 30) {// Web端--30:Web
			platform = 4;
		} else if (clientId == 40) {// H5端--40:H5
			platform = 8;
		} else {
			log.error("[noticeAck] tranceId=[{}], No such clientId=[{}]", tranceId, clientId);
		}
		log.info("[noticeAck_getFromPlatform] tranceId=[{}],fromPlatform=[{}]", tranceId, platform);
		return platform;
	}

}
