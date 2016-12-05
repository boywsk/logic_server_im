package cn.com.gome.logic.worker;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;

import cn.com.gome.logic.dao.GroupMemberDao;
import cn.com.gome.logic.dao.GroupMsgDao;
import cn.com.gome.logic.dao.NoticeMsgDao;
import cn.com.gome.logic.dao.UserRedisDao;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.model.notice.IssueRevokeMsgModel;
import cn.com.gome.logic.model.notice.SaveNoticeMsg;
import cn.com.gome.logic.protobuf.ProtoIM.IssueRevokeMsg;
import cn.com.gome.logic.protobuf.ProtoIM.NoticeMsg;
import cn.com.gome.logic.protobuf.ProtoIM.RevokeMsg;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.utils.CheckClientTypeUtils;
import cn.com.gome.logic.utils.IPUtils;
import cn.com.gome.logic.utils.StringUtils;
import cn.com.gome.logic.utils.TcpDataSendUtils;

public class RevokeMsgWorker implements Runnable {
private static Logger log = LoggerFactory.getLogger(RevokeMsgWorker.class);
	
	private ProtocolPackage msg;
	
	public RevokeMsgWorker(ProtocolPackage msg) {
		this.msg = msg;
	}

	@Override
	public void run() {
		int traceId = msg.getTraceId();
		String appId = msg.getAppId();
		long fromUid = msg.getUid();
		byte clientId = msg.getClientId();
		log.info("[RevokeMsgWorker0] tranceId=[{}],appId=[{}]", traceId, appId);
		long time = System.currentTimeMillis();
		String noticeMsgId = StringUtils.getUuid();
		try {
			byte[] body = msg.getProtoBody();
			RevokeMsg revokeMsg = RevokeMsg.parseFrom(body);
			String groupId = revokeMsg.getGroupId();
			String msgId = revokeMsg.getMsgId();
			log.info("[RevokeMsgWorker1] tranceId=[{}],appId=[{}],groupId=[{}],msgId=[{}]", traceId, appId, groupId, msgId);
			GroupMemberDao memberDao = new GroupMemberDao();
			List<Long> uids = memberDao.listMemberUids(appId, groupId);
			UserRedisDao userRedisDao = new UserRedisDao();
			NoticeMsgDao noticeDao = new NoticeMsgDao();
			
			IssueRevokeMsg.Builder pbIssueRevokeBuilder = IssueRevokeMsg.newBuilder();
			pbIssueRevokeBuilder.setGroupId(groupId);
			pbIssueRevokeBuilder.setMsgId(msgId);
			pbIssueRevokeBuilder.setUid(fromUid);
			//pbIssueRevokeBuilder.setNickName(revokeMsg.getNickName());
			pbIssueRevokeBuilder.setOptTime(time);
			String extra = revokeMsg.getExtra();
			if(!Strings.isNullOrEmpty(extra)) {
				pbIssueRevokeBuilder.setExtra(extra);
			}
			IssueRevokeMsg pbIssueRevoke = pbIssueRevokeBuilder.build();
			NoticeMsg.Builder pbNoticeMsgBuilder = NoticeMsg.newBuilder();
			pbNoticeMsgBuilder.setMsgId(noticeMsgId);
			pbNoticeMsgBuilder.setNoticeType(Constant.NOTICEMSG_TYPE.ISSUEREVOKEMSG.value);
			pbNoticeMsgBuilder.setIssueRevoke(pbIssueRevoke);
			NoticeMsg pbNoticeMsg = pbNoticeMsgBuilder.build();
			
			IssueRevokeMsgModel issueRevoke = new IssueRevokeMsgModel();
			issueRevoke.setUid(fromUid);
			//issueRevoke.setNickName(revokeMsg.getNickName());
			issueRevoke.setMsgId(msgId);
			issueRevoke.setGroupId(groupId);
			issueRevoke.setOptTime(time);
			if(!Strings.isNullOrEmpty(extra)) {
				issueRevoke.setExtra(extra);
			}
			SaveNoticeMsg saveNoticeMsg = new SaveNoticeMsg();
			for (Long uid : uids) {
				//保存撤销消息功能消息
				int platform = userRedisDao.getUserTerminalType(appId, uid);
				saveNoticeMsg.setMsgId(noticeMsgId);
				saveNoticeMsg.setFromUid(fromUid);
				saveNoticeMsg.setToUid(uid);
				saveNoticeMsg.setGroupId(groupId);
				saveNoticeMsg.setNoticeType(Constant.NOTICEMSG_TYPE.ISSUEREVOKEMSG.value);
				saveNoticeMsg.setNoticeMsgJson(JSON.toJSONString(issueRevoke));
				saveNoticeMsg.setSendTime(time);
				if (fromUid == uid) {
					log.info("[RevokeMsgWorker_d] tranceId=[{}],appId=[{}],uid=[{}], platform=[{}]", traceId, appId, uid, platform);
					int revokePlatform = getFromPlatform(clientId,traceId);
					platform = platform ^ revokePlatform;
					log.info("[RevokeMsgWorker_del] tranceId=[{}],appId=[{}],uid=[{}], platform=[{}], revokePlatform=[{}]", traceId, appId, uid, platform, revokePlatform);
				}
				saveNoticeMsg.setPlatform(platform);	
				noticeDao.saveNoticeMsg(saveNoticeMsg, appId, uid, traceId);
				
				Map<String, String> map = userRedisDao.listUserRsp(appId, uid);
				int respSize = map != null ? map.size() : 0;
				log.info("[RevokeMsgWorker] tranceId=[{}],appId=[{}],uid=[{}], user resp size=[{}]", traceId, appId, uid, respSize);
				//转发消息
				if (map != null && !map.isEmpty()) {
					for (String key : map.keySet()) {
						String[] keyArr = key.split("_");
						if (keyArr.length < 2) {
							continue;
						}
						
						byte bclientId = Integer.valueOf(keyArr[0]).byteValue();
						log.info("[RevokeMsgWorker4] tranceId=[{}],appId=[{}],uid=[{}],key=[{}],clientId=[{}]", traceId, appId, uid, key, bclientId);
						if (fromUid == uid && bclientId == clientId) {
							continue;
						}
						String clientKey = map.get(key);
						String[] rspArr = clientKey.split(":");
						long rspIp = Long.valueOf(rspArr[0]);
						int rspport = Integer.parseInt(rspArr[1]);
						log.info("[RevokeMsgWorker5] tranceId=[{}],appId=[{}],uid=[{}],rspIp=[{}],rspport=[{}]",
								traceId, appId, uid, IPUtils.longToIP(rspIp), rspport);
						
						ProtocolPackage pack = msg.deepCopy();
						pack.setAck(Constant.PACK_ACK.NO.value);
						pack.setCommand(Command.CMD_NOTICE_MSG);
						pack.setRspIP(rspIp);
						pack.setRspPort(rspport);
						pack.setReceiveId(uid);
						pack.setClientId(bclientId);
						pack.setProtoBody(pbNoticeMsg.toByteArray());
						log.info("[RevokeMsgWorker6] tranceId=[{}],appId=[{}],uid=[{}],cmd=[{}]]",
								traceId, appId, uid, pack.getCommand());
						TcpDataSendUtils.sendTcpData(clientKey, pack);
					}
				}
			}
			GroupMsgDao groupMsgDao = new GroupMsgDao();
			//判断是否是最后一条，如果是最后一条需要修改t_group表中的lastMsg中的msgStatus字段信息
			groupMsgDao.judgeAndModify(appId, groupId, msgId, traceId);
			//修改状态msgStatus消息状态为1[0:正常、1:撤回、2:删除]
			groupMsgDao.modifyMsgStatusRevoke(appId, groupId, msgId, traceId);
		} catch (InvalidProtocolBufferException e) {
			log.error("[RevokeMsgWorker]:", e);
		}
	}
	/**
	 * 获取平台信息
	 * 
	 * @param clientId
	 * @return
	 */
	public int getFromPlatform(int clientId, long tranceId) {
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
			log.error("[getFromPlatform] tranceId=[{}], No such clientId=[{}]", tranceId, clientId);
		}
		log.info("[getFromPlatform] tranceId=[{}],fromPlatform=[{}]", tranceId, platform);
		return platform;
	}
}
