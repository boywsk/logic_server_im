package cn.com.gome.logic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.dao.GroupMsgDao;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.model.GroupMsg;
import cn.com.gome.logic.pool.ThreadPool;
import cn.com.gome.logic.protobuf.ProtoIM.RevokeMsg;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.worker.RevokeMsgWorker;

public class RevokeMsgService {
	private static Logger log = LoggerFactory.getLogger(RevokeMsgService.class);
	private final long subTime = 2 * 60 *1000;
	
	/**
	 * 撤销消息;并转发
	 */
	public int revokeAndForwardMsg(ProtocolPackage msg) {
		int result = 0;
		int traceId = msg.getTraceId();
		String appId = msg.getAppId();
		long uid = msg.getUid();
		log.info("[RevokeAndForwardMsg] traceId=[{}],appId=[{}],cmd=[{}],uid=[{}]", 
				traceId, appId, msg.getCommand(), uid);
		try {
			byte[] boyd = msg.getProtoBody();
			GroupMsgDao dao = new GroupMsgDao();
			RevokeMsg revokeMsg = RevokeMsg.parseFrom(boyd);
			String groupId = revokeMsg.getGroupId();
			String msgId = revokeMsg.getMsgId();
			GroupMsg groupMsg = dao.getMsg(appId, uid, groupId, msgId, traceId);
			long sendTime = groupMsg.getSendTime();
			long currTime = System.currentTimeMillis();
			ProtocolPackage retMsg = new ProtocolPackage();
			retMsg.setHead(msg.getHead());
			if(currTime - sendTime > subTime) {
				result = Constant.REULT_CODE.REVOEK_MSG_OUTTIME.value;
				return result;
			}
			
			ThreadPool pool = ThreadPool.getInstance();
			RevokeMsgWorker wok = new RevokeMsgWorker(msg);
			pool.addTask(wok);
		} catch (Exception e) {
			log.error("[RevokeAndForwardMsg] cause is:", e);
		}
		return result;
	}
}
