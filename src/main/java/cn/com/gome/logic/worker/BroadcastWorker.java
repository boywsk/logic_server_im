package cn.com.gome.logic.worker;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.dao.GroupDao;
import cn.com.gome.logic.dao.GroupMsgDao;
import cn.com.gome.logic.dao.UserDao;
import cn.com.gome.logic.dao.UserRedisDao;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.model.Group;
import cn.com.gome.logic.model.GroupMsg;
import cn.com.gome.logic.model.User;
import cn.com.gome.logic.mq.MQSender;
import cn.com.gome.logic.pbTools.PackageTools;
import cn.com.gome.logic.pbTools.PbGroupMsgTools;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.protobuf.ProtoIM.ImMsg;
import cn.com.gome.logic.utils.IPUtils;
import cn.com.gome.logic.utils.ProtocolPackageUtils;
import cn.com.gome.logic.utils.TcpDataSendUtils;

/**
 * 全站广播工作线程
 */
public class BroadcastWorker implements Runnable {
	Logger log = LoggerFactory.getLogger(BroadcastWorker.class);
	
	private String appId;
	private GroupMsg groupMsg;
	private boolean isPersist;
	
	public BroadcastWorker(boolean isPersist, String appId, GroupMsg groupMsg) {
		this.appId = appId;
		this.groupMsg = groupMsg;
		this.isPersist = isPersist;
	}
	
	@Override
	public void run() {
		log.info("[BroadcastWorker run] appId=[{}]", appId);
		this.sendMsg();
	}
	
	/**
	 * 发送消息
	 */
	private void sendMsg() {
		ImMsg imMsg = PbGroupMsgTools.groupMsg2PbImMsg(groupMsg);
		ProtocolPackage pack = generatePack(Command.CMD_IM_SEND_MSG, imMsg);
		UserDao userDao = new UserDao();
		UserRedisDao userRedisDao = new UserRedisDao();
		long senderUid = groupMsg.getSenderId();
		int pageSize = 200;
		long lastUid = 0;
		List<User> list = userDao.listUserForPage(appId, lastUid, pageSize);
		while(true) {
			int size = list.size();
			log.info("[sendMsg] user size=[{}]", size);
			lastUid = list.get(size -1).getUid();
			for(User user : list) {
				long uid = user.getUid();
				if(senderUid == uid || uid <= 0) {
					continue;
				}
				
				if (isPersist) {
					String groupId = uid + "_" + groupMsg.getSenderId();
					if(uid > groupMsg.getSenderId()) {
						groupId = groupMsg.getSenderId() + "_" + uid;
					}
					groupMsg.setGroupId(groupId);
					GroupMsgDao msgDao = new GroupMsgDao();
					GroupDao groupDao = new GroupDao();
					Group group = groupDao.incGroupSeq(appId, groupMsg);
					if(group == null) {
						log.error("[saveAndForwardMsg] group is null");
						//pack.setProtoBody(new byte[0]);
						//pack.setResult((byte) -1);
						return;
					}
					groupMsg.setMsgSeqId(group.getSeq());
					msgDao.saveMsg(appId, groupMsg);
				}
				
				Map<String, String> map = userRedisDao.listUserRsp(appId, uid);
				if(map != null && !map.isEmpty()) {
					log.info("[sendMsg] uid=[{}], user resp size=[{}]", uid, map.size());
					for(String key : map.keySet()) {
						String[] keyArr = key.split("_");
						if(keyArr.length < 2) {
							continue;
						}
						ProtocolPackage msg = pack.deepCopy();
						byte bclientId = Integer.valueOf(keyArr[0]).byteValue();
						log.info("[sendMsg] uid=[{}],key=[{}], clientId=[{}]", uid, key, bclientId);
						String clientKey = map.get(key);
						String[] rspArr = clientKey.split(":");
						long rspIp = Long.valueOf(rspArr[0]);
						int rspport = Integer.parseInt(rspArr[1]);
						log.info("[sendMsg] uid=[{}],rspIp=[{}],rspport=[{}]", uid, IPUtils.longToIP(rspIp), rspport);
						ImMsg pbMsg = PbGroupMsgTools.groupMsg2PbImMsg(groupMsg);
						msg.setAck(Constant.PACK_ACK.NO.value);
						msg.setRspIP(rspIp);
						msg.setRspPort(rspport);
						msg.setReceiveId(uid);
						msg.setClientId(bclientId);
						msg.setProtoBody(pbMsg.toByteArray());
						TcpDataSendUtils.sendTcpData(clientKey, msg);
						
						String msgId = groupMsg.getMsgId();
						long fromUid = groupMsg.getSenderId();
						int groupType = groupMsg.getGroupType();
						int msgType =  groupMsg.getMsgType();
						String groupId = groupMsg.getGroupId();
						long currTime = System.currentTimeMillis();
						log.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", "σσmessage", msgId, 
								fromUid, bclientId, appId, groupType, msgType, uid, groupId, currTime);
					}
				} else {//不在线
					if(senderUid != uid) {
						log.info("[sendMsg]=========ios push===========uid=[{}]", uid);
						ProtocolPackage msg = pack.deepCopy();
						msg.setReceiveId(uid);
						byte[] buf = ProtocolPackageUtils.package2Byte(msg);
						MQSender.getInstance().sendMsg(buf);
//						sendPush(appId, uid);
					}
				}
			}
			if(size < pageSize) {
				log.info("[sendMsg] break!!!; user size=[{}]", size);
				break;
			}
			list = userDao.listUserForPage(appId, lastUid, pageSize);
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
//		ByteBuf head = Unpooled.buffer(Global.PACK_HEAD_LENGTH);
//		head.writeShort(0);//encoder计算长度
//		head.writeShort(Command.CMD_IM_SEND_MSG);
//		head.writeLong(imMsg.getSenderId());
//		head.writeByte(0);
//		head.writeByte(0);
//		head.writeByte(0);
//		head.writeByte(0);
//		head.writeLong(System.currentTimeMillis());
//		head.writeLong(0);
//		head.writeLong(0);
//		head.writeInt(0);
//		head.writeInt(0);
//		ByteBuf appIdBuf = Unpooled.buffer(Global.APPID_LENGTH);
//		try {
//			appIdBuf.writeBytes(appId.getBytes("UTF-8"));
//		} catch (UnsupportedEncodingException e) {
//			log.error("[generatePack]:", e);
//		}
//		head.writeBytes(appIdBuf);
//		pack.setHead(head.array());
		
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
	 * 发送push消息
	 * 
	 * @param uid
	 */
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
