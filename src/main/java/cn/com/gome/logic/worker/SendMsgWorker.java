package cn.com.gome.logic.worker;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.com.gome.logic.utils.TcpDataSendUtils;
import cn.com.gome.logic.dao.GroupMemberDao;
import cn.com.gome.logic.dao.UserRedisDao;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.model.GroupMember;
import cn.com.gome.logic.model.GroupMsg;
import cn.com.gome.logic.mq.MQSender;
import cn.com.gome.logic.pbTools.PbGroupMsgTools;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.utils.IPUtils;
import cn.com.gome.logic.utils.ProtocolPackageUtils;
import cn.com.gome.logic.protobuf.ProtoIM.ImMsg;

/**
 * 消息发送工作线程
 */
public class SendMsgWorker implements Runnable {
	static Logger log = LoggerFactory.getLogger(SendMsgWorker.class);

	private ProtocolPackage pack;
	private GroupMsg groupMsg;
	private int groupType;
	private long senderId = 0L;
	private String groupId = null;

	public SendMsgWorker(ProtocolPackage pack, GroupMsg groupMsg, int groupType) {
		this.pack = pack;
		this.groupMsg = groupMsg;
		this.groupType = groupType;
	}

	public void run() {
		log.info("[run] senderId=[{}],senderName=[{}],groupId=[{}],groupType=[{}],tranceId=[{}]", groupMsg.getSenderId(),
				groupMsg.getSenderName(), groupMsg.getGroupId(), groupType, pack.getTraceId());
		groupMsg.setSendTime(System.currentTimeMillis());
		byte clientId = pack.getClientId();
		long senderUid = senderId;
		if (senderUid == 0) {
			senderUid = groupMsg.getSenderId();
		}
		if (null == groupId || groupId.length() <= 0) {
			groupId = groupMsg.getGroupId();
		}
		log.info("[run] chat senderUid=[{}],sender clientId=[{}]", senderUid, clientId);
		try {
			if (groupType == Constant.CHAT_TYPE.SINGLE.value) {//单聊
				log.info("single msg!!!,,tranceId=[{}]", pack.getTraceId());
				sendSingleChat(clientId, groupMsg);
			} else if (groupType == Constant.CHAT_TYPE.GROUP.value) {// 群聊
				log.info("group msg!!!,tranceId=[{}]", pack.getTraceId());
				sendGroupChat(clientId, groupMsg);
			}else if (groupType == Constant.CHAT_TYPE.SYS.value) {// 群聊
				log.info("system msg!!!");
			}
		} catch (Exception e) {
			log.error("[run] cause is:", e);
		}
	}

	/**
	 * 发送一对一聊天信息
	 * 
	 * @param clientId
	 * @param groupMsg
	 */
	private void sendSingleChat(byte clientId, GroupMsg groupMsg) {
		try {
			String appId = pack.getAppId().trim();
			long senderUid = groupMsg.getSenderId();
			String groupId = groupMsg.getGroupId();
			int tranceId = pack.getTraceId();
			long currTime = System.currentTimeMillis();
			String msgId = groupMsg.getMsgId();
			long fromUid = groupMsg.getSenderId();
			int groupType = groupMsg.getGroupType();
			int msgType =  groupMsg.getMsgType();
			log.info("[sendSingleChat] appId=[{}],senderUid=[{}],groupId=[{}],,tranceId=[{}]",
					appId, senderUid, groupId, pack.getTraceId());
			String[] uidArr = groupId.split("_");
			UserRedisDao userRedisDao = new UserRedisDao();
			for (int i = 0; i < uidArr.length; i++) {
				long uid = Long.valueOf(uidArr[i]);
				Map<String, String> map = userRedisDao.listUserRsp(appId, uid);
				if (map != null && !map.isEmpty()) {
					log.info("[sendSingleChat] chat uid=[{}],resp size=[{}],tranceId=[{}]", uid, map.size(), tranceId);
					for (String key : map.keySet()) {
						String[] keyArr = key.split("_");
						if (keyArr.length < 2) {
							continue;
						}
						byte bClientId = Integer.valueOf(keyArr[0]).byteValue();
						log.info("[sendSingleChat] chat uid=[{}],key=[{}],bClientId=[{}],tranceId=[{}]", uid, key, bClientId, tranceId);
						if(senderUid == uid && bClientId == clientId) {
							log.info("[sendSingleChat self] uid=[{}],clientId=[{}],bClientId=[{}],tranceId=[{}]", uid, clientId, bClientId, tranceId);
							
							continue;
						}
						ProtocolPackage msg = pack.deepCopy();
						String clientKey = map.get(key);
						String[] rspArr = clientKey.split(":");
						long rspIp = Long.valueOf(rspArr[0]);
						int rspport = Integer.parseInt(rspArr[1]);
						log.info("[sendSingleChat] chat uid=[{}],rspIp=[{}],rspport=[{}],tranceId=[{}]", uid, IPUtils.longToIP(rspIp), rspport, tranceId);
						ImMsg pbMsg = PbGroupMsgTools.groupMsg2PbImMsg(groupMsg);
						msg.setAck(Constant.PACK_ACK.NO.value);
						msg.setRspIP(rspIp);
						msg.setRspPort(rspport);
						msg.setReceiveId(uid);
						msg.setClientId(bClientId);
						msg.setProtoBody(pbMsg.toByteArray());
						TcpDataSendUtils.sendTcpData(clientKey, msg);
						
						log.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", "σσmessage", msgId, 
								fromUid, bClientId, appId, groupType, msgType, uid, groupId, currTime);
					}
				} else {// 不在线
					if (senderUid != uid) {
						ProtocolPackage msg = pack.deepCopy();
						log.info("[sendSingleChat]=========ios push==========senderUid=[{}],uid=[{}]", senderUid, uid);
						log.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", "σσmessage", msgId, 
								fromUid, -1, appId, groupType, msgType, uid, groupId, currTime);
						msg.setReceiveId(uid);
						byte[] buf = ProtocolPackageUtils.package2Byte(msg);
						MQSender.getInstance().sendMsg(buf);
					}
				}
			}
		} catch (Exception e) {
			log.error("[sendSingleChat]:", e);
		}
	}

	/**
	 * 发群聊天信息
	 * 
	 * @param clientId
	 * @param groupMsg
	 */
	private void sendGroupChat(byte clientId, GroupMsg groupMsg) {
		try {
			String appId = pack.getAppId().trim();
			int tranceId = pack.getTraceId();
			long senderUid = groupMsg.getSenderId();
			String groupId = groupMsg.getGroupId();
			String msgId = groupMsg.getMsgId();
			long fromUid = groupMsg.getSenderId();
			int groupType = groupMsg.getGroupType();
			int msgType =  groupMsg.getMsgType();
			long currTime = System.currentTimeMillis();
			UserRedisDao userRedisDao = new UserRedisDao();
			GroupMemberDao memberDao = new GroupMemberDao();
			List<GroupMember> list = memberDao.listGroupMember(appId, groupId);// 获取审核通过的群组成员
			log.info("[sendGroupChat] members size=[{}],tranceId=[{}]", list.size(), tranceId);
			log.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", "σσmessage", msgId, 
					fromUid, clientId, appId, groupType, msgType, 0, groupId, currTime);
			for (GroupMember member : list) {
				long uid = member.getUid();
				log.info("[sendGroupChat] uid=[{}],tranceId=[{}]", uid, tranceId);
				Map<String, String> map = userRedisDao.listUserRsp(appId, uid);
				int respSize = map != null ? map.size() : 0;
				log.info("[sendGroupChat] uid=[{}], user resp size=[{}],tranceId=[{}]", uid, respSize, tranceId);
				if (map != null && !map.isEmpty()) {
					for (String key : map.keySet()) {
						String[] keyArr = key.split("_");
						if (keyArr.length < 2) {
							continue;
						}
						ProtocolPackage msg = pack.deepCopy();
						byte bclientId = Integer.valueOf(keyArr[0]).byteValue();
						log.info("[sendGroupChat] uid=[{}],key=[{}],clientId=[{}],tranceId=[{}]", uid, key, bclientId, tranceId);
						if(senderUid == uid && bclientId == clientId) {
							log.info("[sendGroupChat self] uid=[{}],clientId=[{}],bclientId=[{}],tranceId=[{}]",
									uid, clientId, bclientId, tranceId);
							continue;
						}
						String clientKey = map.get(key);
						String[] rspArr = clientKey.split(":");
						long rspIp = Long.valueOf(rspArr[0]);
						int rspport = Integer.parseInt(rspArr[1]);
						log.info("[sendGroupChat] uid=[{}],rspIp=[{}],rspport=[{}],tranceId=[{}]",
								uid, IPUtils.longToIP(rspIp), rspport, tranceId);
						ImMsg pbMsg = PbGroupMsgTools.groupMsg2PbImMsg(groupMsg);
						msg.setAck(Constant.PACK_ACK.NO.value);
						msg.setRspIP(rspIp);
						msg.setRspPort(rspport);
						msg.setReceiveId(uid);
						msg.setClientId(bclientId);
						msg.setProtoBody(pbMsg.toByteArray());
						TcpDataSendUtils.sendTcpData(clientKey, msg);
						
						log.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", "σσmessage", msgId, 
								fromUid, clientId, appId, groupType, msgType, uid, groupId, currTime);
					}
				} else {// 不在线
					if (senderUid != uid) {
						ProtocolPackage msg = pack.deepCopy();
						msg.setReceiveId(uid);
						log.info("[sendGroupChat]=========ios push==========senderUid=[{}],uid=[{}]", senderUid, uid);
						byte[] buf = ProtocolPackageUtils.package2Byte(msg);
						MQSender.getInstance().sendMsg(buf);
					}
				}
			}
		} catch (Exception e) {
			log.error("[sendGroupChat]", e);
		}
	}

}
