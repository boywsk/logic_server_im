package cn.com.gome.logic.worker;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.utils.TcpDataSendUtils;
import cn.com.gome.logic.dao.GroupMemberDao;
import cn.com.gome.logic.dao.UserRedisDao;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.model.GroupMember;
import cn.com.gome.logic.model.GroupMsg;
import cn.com.gome.logic.mq.MQSender;
import cn.com.gome.logic.pbTools.PackageTools;
import cn.com.gome.logic.pbTools.PbGroupMsgTools;
import cn.com.gome.logic.protobuf.ProtoIM.ImMsg;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.utils.IPUtils;
import cn.com.gome.logic.utils.ProtocolPackageUtils;

/**
 * 发送通过api接口发上来的消息工作线程
 */
public class SendApiMsgWork implements Runnable {
	static Logger log = LoggerFactory.getLogger(SendApiMsgWork.class);
	
	private GroupMsg groupMsg;
	private String appId;
	private boolean containSelf;
	private boolean isMsgBlocked;
	
	public SendApiMsgWork(String appId, GroupMsg groupMsg, boolean containSelf, boolean isMsgBlocked) {
		this.appId = appId;
		this.groupMsg = groupMsg;
		this.containSelf = containSelf;
		this.isMsgBlocked = isMsgBlocked;
	}
	
	public void run() {
		int groupType = groupMsg.getGroupType();
		try {
			//单聊
			if(groupType == Constant.CHAT_TYPE.SINGLE.value) {
				sendSingleChat(appId, groupMsg);
			} else if(groupType == Constant.CHAT_TYPE.GROUP.value) {//群聊
				sendGroupChat(appId, groupMsg);
			}
		} catch (Exception e) {
			log.error("[run] cause is:", e);
		}
	}
	
	/**
	 * 生成pack
	 * @param cmd
	 * @param receiverId
	 * @param body
	 * @return
	 */
	private ProtocolPackage generatePack(short cmd, ImMsg imMsg) {
		log.info("[generatePack] cmd=[{}],groupId=[{}]", cmd, imMsg.getGroupId());
		ProtocolPackage pack = new ProtocolPackage();
		byte[] head = PackageTools.generateHead(appId, imMsg.getSenderId(), Command.CMD_IM_SEND_MSG);
		pack.setHead(head);
		pack.setStime(System.currentTimeMillis());
		pack.setUid(imMsg.getSenderId());
		pack.setCommand(cmd);
		pack.setAppId(appId);
		if(imMsg != null) {
			pack.setProtoBody(imMsg.toByteArray());
		}
		return pack;
	}
	
	/**
	 * 发送一对一聊天信息
	 * @param clientId
	 * @param groupMsg
	 */
	private void sendSingleChat(String appId, GroupMsg groupMsg) {
		log.info("[sendSingleChat] isMsgBlocked=[{}]", isMsgBlocked);
		try {
			ImMsg imMsg = PbGroupMsgTools.groupMsg2PbImMsg(groupMsg);
			ProtocolPackage pack = generatePack(Command.CMD_IM_SEND_MSG, imMsg);
			pack.setAppId(appId);
			long senderUid = groupMsg.getSenderId();
			String groupId = groupMsg.getGroupId();
			String[] uidArr = groupId.split("_");
			String msgId = groupMsg.getMsgId();
			long fromUid = groupMsg.getSenderId();
			int groupType = groupMsg.getGroupType();
			int msgType =  groupMsg.getMsgType();
			long currTime = System.currentTimeMillis();
			
			log.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", "σσmessage", msgId, 
					fromUid, 0, appId, groupType, msgType, 0, groupId, currTime);
			UserRedisDao userRedisDao = new UserRedisDao();
			for(int i = 0; i < uidArr.length; i++) {
				long uid = Long.valueOf(uidArr[i]);
				Map<String, String> map = userRedisDao.listUserRsp(appId, uid);
				if(senderUid == uid && !containSelf) {//不发送给自己
					continue;
				}
				if(map != null && !map.isEmpty()) {
					log.info("[sendSingleChat] chat uid=[{}],resp size=[{}]", uid, map.size());
					for(String key : map.keySet()) {
						String[] keyArr = key.split("_");
						if(keyArr.length < 2) {
							continue;
						}
						byte bClientId = Integer.valueOf(keyArr[0]).byteValue();
						log.info("[sendSingleChat] chat uid=[{}],key=[{}],bClientId=[{}]", uid, key, bClientId);
						String clientKey = map.get(key);
						String[] rspArr = clientKey.split(":");
						long rspIp = Long.valueOf(rspArr[0]);
						int rspport = Integer.parseInt(rspArr[1]);
						log.info("[sendSingleChat] chat uid=[{}],rspIp=[{}],rspport=[{}]", uid, IPUtils.longToIP(rspIp), rspport);
						ImMsg pbMsg = PbGroupMsgTools.groupMsg2PbImMsg(groupMsg);
						if(isMsgBlocked) {
							pbMsg = pbMsg.newBuilder(pbMsg).setPushStatus(1).build();
						} else {
							pbMsg = pbMsg.newBuilder(pbMsg).setPushStatus(0).build();
						}
						
						pack.setAck(Constant.PACK_ACK.NO.value);
						pack.setRspIP(rspIp);
						pack.setRspPort(rspport);
						pack.setReceiveId(uid);
						pack.setClientId(bClientId);
						pack.setProtoBody(pbMsg.toByteArray());
						log.info("[sendSingleChat] msgBody=[{}]", pbMsg.getMsgBody());
						TcpDataSendUtils.sendTcpData(clientKey, pack);
						
						log.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", "σσmessage", msgId, 
								fromUid, bClientId, appId, groupType, msgType, uid, groupId, currTime);
					}
				} else {//不在线
					log.info("[sendSingleChat] 2 isMsgBlocked=[{}]", isMsgBlocked);
					if(isMsgBlocked) {
						continue;
					}
					if(senderUid != uid) {
						log.info("[sendSingleChat]=========ios push===========uid=[{}]", uid);
						pack.setReceiveId(uid);
						byte[] buf = ProtocolPackageUtils.package2Byte(pack);
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
	 * @param clientId
	 * @param groupMsg
	 */
	private void sendGroupChat(String appId, GroupMsg groupMsg) {
		log.info("[sendGroupChat] isMsgBlocked=[{}]", isMsgBlocked);
		try {
			ImMsg imMsg = PbGroupMsgTools.groupMsg2PbImMsg(groupMsg);
			ProtocolPackage pack = generatePack(Command.CMD_IM_SEND_MSG, imMsg);
			long senderUid = groupMsg.getSenderId();
			String groupId = groupMsg.getGroupId();
			UserRedisDao userRedisDao = new UserRedisDao();
			GroupMemberDao memberDao = new GroupMemberDao();
			List<GroupMember> list = memberDao.listGroupMember(appId, groupId);
			for(GroupMember member : list) {
				long uid = member.getUid();
				Map<String, String> map = userRedisDao.listUserRsp(appId, uid);
				if(map != null && !map.isEmpty()) {
					log.info("[sendGroupChat] group chat uid=[{}], user resp size=[{}]", uid, map.size());
					for(String key : map.keySet()) {
						String[] keyArr = key.split("_");
						if(keyArr.length < 2) {
							continue;
						}
						byte bClientId = Integer.valueOf(keyArr[0]).byteValue();
						log.info("[sendGroupChat] group chat uid=[{}],key=[{}], clientId=[{}]", uid, key, bClientId);
						String clientKey = map.get(key);
						String[] rspArr = clientKey.split(":");
						long rspIp = Long.valueOf(rspArr[0]);
						int rspport = Integer.valueOf(rspArr[1]);
						log.info("[sendGroupChat] group chat uid=[{}],rspIp=[{}],rspport=[{}]", uid, IPUtils.longToIP(rspIp), rspport);
						ImMsg pbMsg = PbGroupMsgTools.groupMsg2PbImMsg(groupMsg);
						if(isMsgBlocked) {
							pbMsg = pbMsg.newBuilder(pbMsg).setPushStatus(1).build();
						} else {
							pbMsg = pbMsg.newBuilder(pbMsg).setPushStatus(0).build();
						}
						pack.setAck(Constant.PACK_ACK.NO.value);
						pack.setRspIP(rspIp);
						pack.setRspPort(rspport);
						pack.setReceiveId(uid);
						pack.setClientId(bClientId);
						pack.setProtoBody(pbMsg.toByteArray());
						log.info("[sendGroupChat] msgBody=[{}]", pbMsg.getMsgBody());
						TcpDataSendUtils.sendTcpData(clientKey, pack);
						
						String msgId = groupMsg.getMsgId();
						long fromUid = groupMsg.getSenderId();
						int groupType = groupMsg.getGroupType();
						int msgType =  groupMsg.getMsgType();
						long currTime = System.currentTimeMillis();
						log.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", "σσmessage", msgId, 
								fromUid, bClientId, appId, groupType, msgType, uid, groupId, currTime);
					}
				} else {//不在线
					log.info("[sendGroupChat] 2 isMsgBlocked=[{}]", isMsgBlocked);
					if(isMsgBlocked) {
						continue;
					}
					if(senderUid != uid) {
						log.info("[sendGroupChat]=========ios push===========uid=[{}]", uid);
						pack.setReceiveId(uid);
						byte[] buf = ProtocolPackageUtils.package2Byte(pack);
						MQSender.getInstance().sendMsg(buf);
					}
				}
			}
		} catch (Exception e) {
			log.error("[sendGroupChat]:", e);
		}
	}
	
//	/**
//	 * 发送push消息
//	 * @param uid
//	 */
//	private void sendPush(String appId, long uid) {
//		log.info("[sendPush] appId=[{}],uid=[{}]", appId, uid);
//		UserDao dao = new UserDao();
//		// ios
//		User user = dao.getIOSUser(appId, uid);
//		if(user == null) {
//			log.error("[sendPush] user is null or not ios!!!");
//			return;
//		}
//		String apnsToken = user.getApnsToken();
//		if (apnsToken != null && apnsToken.length() > 0) {
//			log.info("[sendPush] appId=[{}],uid=[{}],=apnsToken=[{}]", appId, uid, apnsToken);
//			ApnsPushManager pushService = new ApnsPushManager();
//			pushService.push(appId, apnsToken, uid, groupMsg);
//		} else {
//			log.error("[push] token is null or empty!!!");
//			return;
//		}
//	}
}
