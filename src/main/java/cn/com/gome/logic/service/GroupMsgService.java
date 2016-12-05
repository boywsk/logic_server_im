package cn.com.gome.logic.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.utils.TcpDataSendUtils;
import cn.com.gome.logic.dao.GroupDao;
import cn.com.gome.logic.dao.GroupMemberDao;
import cn.com.gome.logic.dao.GroupMsgDao;
import cn.com.gome.logic.dao.UserRedisDao;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.model.ConsultMsg;
import cn.com.gome.logic.model.Group;
import cn.com.gome.logic.model.GroupMember;
import cn.com.gome.logic.model.GroupMsg;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.pbTools.PbGroupMsgTools;
import cn.com.gome.logic.pool.ThreadPool;
import cn.com.gome.logic.protobuf.ProtoIM.ConsultImMsg;
import cn.com.gome.logic.protobuf.ProtoIM.ImGroup;
import cn.com.gome.logic.protobuf.ProtoIM.ImMsg;
import cn.com.gome.logic.protobuf.ProtoIM.IssueReadSeqMsg;
import cn.com.gome.logic.protobuf.ProtoIM.SubmitInitSeqMsg;
import cn.com.gome.logic.protobuf.ProtoIM.SubmitReadSeqMsg;
import cn.com.gome.logic.protobuf.ProtoIM.UserData;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.worker.BroadcastWorker;
import cn.com.gome.logic.worker.SendApiMsgWork;
import cn.com.gome.logic.worker.SendConsultMsgWorker;
import cn.com.gome.logic.worker.SendMsgWorker;

/**
 * 聊天消息相关业务操作
 */
public class GroupMsgService {

	private static Logger log = LoggerFactory.getLogger(GroupMsgService.class);
	/**
	 * 保存聊天消息；并转发
	 * 
	 * @param msg
	 */
	public ProtocolPackage saveAndForwardMsg(ProtocolPackage msg) {
		log.info("[saveAndForwardMsg] cmd=[{}],uid=[{}],tranceId=[{}]", msg.getCommand(), msg.getUid(), msg.getTraceId());
//		ProtocolPackage pack = msg;
		ProtocolPackage pack = msg.deepCopy();
		GroupMsgDao msgDao = new GroupMsgDao();
		GroupDao groupDao = new GroupDao();
		try {
			String appId = msg.getAppId().trim();
			byte[] body = msg.getProtoBody();
			ImMsg pbMsg = ImMsg.parseFrom(body);
			log.info("[saveAndForwardMsg] cmd=[{}],senderId=[{}],senderName=[{}],groupId=[{}],tranceId=[{}]", msg.getCommand(),
					pbMsg.getSenderId(), pbMsg.getSenderName(), pbMsg.getGroupId(), msg.getTraceId());
			GroupMsg groupMsg = PbGroupMsgTools.PbImMsg2GroupMsg(pbMsg);
			groupMsg.setSendTime(System.currentTimeMillis());
			Group group = groupDao.incGroupSeq(appId, groupMsg);
			if(group == null) {
				log.error("[saveAndForwardMsg] group is null");
				pack.setProtoBody(new byte[0]);
				pack.setResult((byte) -1);
			}
			groupMsg.setMsgSeqId(group.getSeq());
			log.info("[saveAndForwardMsg] senderName=[{}],tranceId=[{}]", groupMsg.getSenderName(), msg.getTraceId());
			msgDao.saveMsg(appId, groupMsg);
			
			// 转发消息；异步
			ThreadPool pool = ThreadPool.getInstance();
			SendMsgWorker worker = new SendMsgWorker(msg, groupMsg, group.getType());
			pool.addTask(worker);
			int capacity = pool.getRemainingCapacity();
			log.info("[saveAndForwardMsg] capacity=[{}]", capacity);
			
//			int groupType = groupMsg.getGroupType();
//			byte clientId = pack.getClientId();
//			if (groupType == Constant.CHAT_TYPE.SINGLE.value) {//单聊
//				log.info("single msg!!!");
//				sendSingleChat(clientId, groupMsg, msg);
//			} else if (groupType == Constant.CHAT_TYPE.GROUP.value) {// 群聊
//				log.info("group msg!!!");
//				sendGroupChat(clientId, groupMsg, msg);
//			}else if (groupType == Constant.CHAT_TYPE.SYS.value) {// 群聊
//				log.info("system msg!!!");
//			}
			// 封装返回包
			GroupMsg retMsg = new GroupMsg();
			retMsg.setGroupId(groupMsg.getGroupId());
			retMsg.setGroupType(groupMsg.getGroupType());
			retMsg.setMsgSeqId(groupMsg.getMsgSeqId());
			retMsg.setMsgId(groupMsg.getMsgId());
			retMsg.setSendTime(System.currentTimeMillis());
			ImMsg retPbMsg = PbGroupMsgTools.groupMsg2PbImMsg(retMsg);
			pack.setProtoBody(retPbMsg.toByteArray());
		} catch (Exception e) {
			log.error("[saveAndForwardMsg] cause is:", e);
			pack.setProtoBody(new byte[0]);
			pack.setResult((byte) -1);
		}

		return pack;
	}

	/**
	 * 分页获取聊天消息
	 * 
	 * @param groupId
	 * @param seqId
	 * @param size
	 * @return
	 */
	public List<UserData> listMsg(String appId, String groupId, long uid, long seqId, int size) {
		log.info("[listMsg] groupId=[{}],uid=[{}],seqId=[{}],pageSize=[{}]", groupId, uid, seqId, size);
		List<UserData> dataList = new ArrayList<UserData>();
		UserData.Builder pbMsgList = UserData.newBuilder();
		GroupMsgDao dao = new GroupMsgDao();
		long initSeq = 0;
		GroupMemberDao memberDao = new GroupMemberDao();
		GroupMember member = memberDao.getMemberByGroupIdAndUid(appId, groupId, uid);
		if(member != null) {
			initSeq = member.getInitSeq();
		}
		if(seqId < initSeq) {
			return dataList;
		}
		long size2 = (long)size;
		if(seqId - initSeq  <  size2) {
			size = (int)(seqId - initSeq);
		}
		log.info("[listMsg] groupId=[{}],uid=[{}],seqId=[{}],calculation size=[{}]", groupId, uid, seqId, size);
		List<GroupMsg> list = dao.listGroupMsg(appId, groupId, seqId, size);
		if (list != null) {
			int listSize = list.size();
			log.info("[listMsg] appId=[{}],groupId=[{}],uid=[{}],listSize=[{}]",
					appId, groupId, uid, listSize);
			for (GroupMsg msg : list) {
				int pbMsgListLength = pbMsgList.build().getSerializedSize();
				ImMsg pbMsg = PbGroupMsgTools.groupMsg2PbImMsg(msg);
				int pbSize = pbMsg.getSerializedSize();
				int totalSize = pbMsgListLength + pbSize;
				log.info("[listMsg] appId=[{}],groupId=[{}],uid=[{}],pbMsgListLength=[{}],pbSize=[{}],totalSize=[{}]",
						appId, groupId, uid, pbMsgListLength, pbSize, totalSize);
				if(pbSize > Global.MAX_PACK_SIZE) {
					log.error("[listGroupByUid] ImMsg is lage!!!!!!!!!!!!!!!!!!!,pbSize=[{}]", pbSize);
					continue;
				}
				
				if(totalSize > Global.MAX_PACK_SIZE) {
					dataList.add(pbMsgList.build());
					pbMsgList = UserData.newBuilder();
				}
				pbMsgList.addMsg(pbMsg);
				
//				if((pbMsgListLength >= Global.MIN_PACK_SIZE && pbMsgListLength <= Global.MAX_PACK_SIZE) ||
//						(totalSize >= Global.MIN_PACK_SIZE && totalSize <= Global.MAX_PACK_SIZE)) {
//					dataList.add(pbMsgList.build());
//					pbMsgList = UserData.newBuilder();
//				}
//				pbMsgList.addMsg(pbMsg);
			}
			dataList.add(pbMsgList.build());
		}
		log.info("[listMsg] groupId=[{}],uid=[{}],seqId=[{}],dataList size=[{}]", groupId, uid, seqId, dataList.size());

		return dataList;
	}

	/**
	 * 修改用户群已读seq；并转发给在线另一终端类型
	 * 
	 * @param msg
	 */
	public void upateReadSeq(String clientKey, ProtocolPackage msg) {
		int tranceId = msg.getTraceId();
		GroupMemberDao memberDao = new GroupMemberDao();
//		SystemGroupMemberDao systemGroupMemberDao = new SystemGroupMemberDao();
		UserRedisDao redisDao = new UserRedisDao();
		try {
			byte[] body = msg.getProtoBody();
			SubmitReadSeqMsg pbMsg = SubmitReadSeqMsg.parseFrom(body);
			String appId = msg.getAppId().trim();
			long uid = pbMsg.getUid();
			log.info("[upateReadSeq] appId=[{}],tranceId=[{}],uid=[{}]", appId, tranceId, uid);
			// byte clientId = msg.getClientId();
			IssueReadSeqMsg.Builder pbIssueMsgBuilder = IssueReadSeqMsg.newBuilder();
			pbIssueMsgBuilder.setUid(uid);
			if (pbMsg.getGroupCount() > 0) {
				List<ImGroup> list = pbMsg.getGroupList();
				for (ImGroup pbGroup : list) {
					String groupId = pbGroup.getGroupId();
//					int groupType = pbGroup.getGroupType();
					long readSeq = pbGroup.getReadSeqId();
					pbIssueMsgBuilder.addGroup(pbGroup);
					log.info("[upateReadSeq] appId=[{}],tranceId=[{}],groupId=[{}],uid=[{}],readSeqId=[{}]",
							appId, tranceId, groupId, uid, readSeq);
//					if (groupType == Constant.CHAT_TYPE.GROUP.value || groupType == Constant.CHAT_TYPE.SINGLE.value) {
					memberDao.updateMemberReadSeq(appId, groupId, uid, readSeq);
//					} else if (groupType == Constant.CHAT_TYPE.SYS.value) {// 系统消息
//						systemGroupMemberDao.updateMemberReadSeq(appId, groupId, uid, readSeq);
//					}
				}
			}
			// 同步到在线另一终端
			Map<String, String> map = redisDao.listUserRsp(appId, uid);
			if (map != null) {
				for (String key : map.keySet()) {
					String[] keyArr = key.split("_");
					if (keyArr.length < 2) {
						continue;
					}
					byte bClientId = Integer.valueOf(keyArr[0]).byteValue();
					// 顺便给自己也下发一份
					// if (!keyArr[0].startsWith(startFlag)) {
					String rspValue = map.get(key);
					String[] valueArr = rspValue.split(":");
					if (valueArr.length >= 2) {
						long rspIp = Long.valueOf(valueArr[0]);
						int rspport = Integer.parseInt(valueArr[1]);
						if (rspport > 0) {
							IssueReadSeqMsg pbIssueMsg = pbIssueMsgBuilder.build();
							if (pbIssueMsg.getGroupCount() > 0) {
//								ProtocolPackage pack = new ProtocolPackage();
								ProtocolPackage pack = msg.deepCopy();
								pack.setAck(Constant.PACK_ACK.YES.value);
//								pack.setHead(msg.getHead());
								pack.setCommand(Command.CMD_ISSUE_READ_SEQ);
								pack.setProtoBody(pbIssueMsg.toByteArray());
								pack.setRspIP(rspIp);
								pack.setRspPort(rspport);
								pack.setClientId(bClientId);
								pack.setReceiveId(uid);
								log.info("[upateReadSeq] appId=[{}],tranceId=[{}],uid=[{}],clientid=[{}]",
										appId, tranceId, uid, bClientId);
								// 发送消息
								TcpDataSendUtils.sendTcpData(clientKey, pack);
							}
						}
					}
					// }
				}
			}
		} catch (Exception e) {
			log.error("[upateReadSeq] cause is:", e);
		}
	}
	
	/**
	 * 修改用户群initseq；并转发给在线另一终端类型
	 * 
	 * @param msg
	 */
	public void upateInitSeq(String clientKey, ProtocolPackage msg) {
		GroupMemberDao memberDao = new GroupMemberDao();
		try {
			byte[] body = msg.getProtoBody();
			SubmitInitSeqMsg pbMsg = SubmitInitSeqMsg.parseFrom(body);
			String appId = msg.getAppId().trim();
			long uid = pbMsg.getUid();
			log.info("[upateInitSeq] appId=[{}],uid=[{}]", appId, uid);
			// byte clientId = msg.getClientId();
			IssueReadSeqMsg.Builder pbIssueMsgBuilder = IssueReadSeqMsg.newBuilder();
			pbIssueMsgBuilder.setUid(uid);
			if (pbMsg.getGroupCount() > 0) {
				List<ImGroup> list = pbMsg.getGroupList();
				for (ImGroup pbGroup : list) {
					String groupId = pbGroup.getGroupId();
//					int groupType = pbGroup.getGroupType();
					long initSeq = pbGroup.getInitSeqId();
					pbIssueMsgBuilder.addGroup(pbGroup);
					log.info("[upateInitSeq] appId=[{}],groupId=[{}],uid=[{}],initSeq=[{}]", appId, groupId, uid,
							initSeq);
					//if (groupType == Constant.CHAT_TYPE.GROUP.value || groupType == Constant.CHAT_TYPE.SINGLE.value) {
						memberDao.updateMemberInitSeq(appId, groupId, uid, initSeq);
					//}
				}
			}
//			// 同步到在线另一终端
//			Map<String, String> map = redisDao.listUserRsp(appId, uid);
//			if (map != null) {
//				for (String key : map.keySet()) {
//					String[] keyArr = key.split("_");
//					if (keyArr.length < 2) {
//						continue;
//					}
//					byte bClientId = Integer.valueOf(keyArr[0]).byteValue();
//					// 顺便给自己也下发一份
//					// if (!keyArr[0].startsWith(startFlag)) {
//					String rspValue = map.get(key);
//					String[] valueArr = rspValue.split(":");
//					if (valueArr.length >= 2) {
//						long rspIp = Long.valueOf(valueArr[0]);
//						int rspport = Integer.parseInt(valueArr[1]);
//						if (rspport > 0) {
//							IssueReadSeqMsg pbIssueMsg = pbIssueMsgBuilder.build();
//							if (pbIssueMsg.getGroupCount() > 0) {
//								ProtocolPackage pack = new ProtocolPackage();
//								pack.setAck(Constant.PACK_ACK.YES.value);
//								pack.setHead(msg.getHead());
//								pack.setCommand(Command.CMD_ISSUE_READ_SEQ);
//								pack.setProtoBody(pbIssueMsg.toByteArray());
//								pack.setRspIP(rspIp);
//								pack.setRspPort(rspport);
//								pack.setClientId(bClientId);
//								pack.setReceiveId(uid);
//								log.info("[upateInitSeq] appId=[{}],uid=[{}],clientid=[{}]", appId, uid, bClientId);
//								// 发送消息
//								TcpDataSendUtils.sendTcpData(clientKey, pack);
//							}
//						}
//					}
//					// }
//				}
//			}
		} catch (Exception e) {
			log.error("[upateInitSeq] cause is:", e);
		}
	}

	/**
	 * 保存聊天消息；并转发
	 * 
	 * @param msg
	 */
	public void saveAndForwardApiMsg(MQMsg msg) {
		String appId = msg.getAppId().trim();
		boolean isPersist = msg.isPersist();
		GroupMsg groupMsg = msg.getGroupMsg();
		boolean isMsgBlocked  = msg.isMsgBlocked();
		log.info("[saveAndForwardApiMsg] appId=[{}],uid=[{}],isPersist=[{}],isMsgBlocked=[{}]",
				appId, groupMsg.getSenderId(), isPersist, isMsgBlocked);
		try {
			if (isPersist) {
				GroupMsgDao msgDao = new GroupMsgDao();
				GroupDao groupDao = new GroupDao();
				Group group = groupDao.incGroupSeq(appId, groupMsg);
				if(group == null) {
					log.error("[saveAndForwardApiMsg] group is null");
					return;
				}
				groupMsg.setMsgSeqId(group.getSeq());
				msgDao.saveMsg(appId, groupMsg);
			}
			boolean containSelf = msg.getContainSelf();
			// 转发消息；异步
			ThreadPool pool = ThreadPool.getInstance();
			SendApiMsgWork worker = new SendApiMsgWork(appId, groupMsg, containSelf, isMsgBlocked);
			pool.addTask(worker);
			int capacity = pool.getRemainingCapacity();
			log.info("[saveAndForwardApiMsg] capacity=[{}]", capacity);
		} catch (Exception e) {
			log.error("[saveAndForwardApiMsg] cause is:", e);
		}
	}

	/**
	 * 全站广播消息
	 * 
	 * @param groupMsg
	 */
	public void broadcastMsg(String appId, boolean isPersist, GroupMsg groupMsg) {
		log.info("[broadcastMsg] appId=[{}],uid=[{}],isPersist=[{}]", appId, groupMsg.getSenderId(), isPersist);
		// 广播消息；异步
		ThreadPool pool = ThreadPool.getInstance();
		BroadcastWorker worker = new BroadcastWorker(isPersist, appId, groupMsg);
		pool.addTask(worker);
		int capacity = pool.getRemainingCapacity();
		log.info("[broadcastMsg] capacity=[{}]", capacity);
	}

	/**
	 * 客服信息
	 * 
	 * @param msg
	 */
	public ProtocolPackage consultMsg(ProtocolPackage msg) {
		log.info("[consultMsg] cmd=[{}],uid=[{}]", msg.getCommand(), msg.getUid());
		ProtocolPackage pack = msg.deepCopy();
		GroupMsgDao msgDao = new GroupMsgDao();
		GroupDao groupDao = new GroupDao();
		try {
			String appId = msg.getAppId().trim();
			byte[] body = msg.getProtoBody();
			ConsultImMsg pbConsultImMsg = ConsultImMsg.parseFrom(body);
			ConsultMsg consultMsg = PbGroupMsgTools.pb2ConsultMsg(pbConsultImMsg);
			long customerId = consultMsg.getCustomerId();
			String shopId = consultMsg.getShopId();
			String consultMsgExtra = consultMsg.getExtra();
			ImMsg pbMsg = pbConsultImMsg.getImMsg();
			log.info("[consultMsg] appId=[{}],cmd=[{}],senderId=[{}],senderName=[{}],groupId=[{}]", appId,
					msg.getCommand(),pbMsg.getSenderId(), pbMsg.getSenderName(), pbMsg.getGroupId());
			GroupMsg groupMsg = PbGroupMsgTools.PbImMsg2GroupMsg(pbMsg);
			groupMsg.setSendTime(System.currentTimeMillis());
			Group group = groupDao.incGroupSeq(appId, groupMsg);
			if(group == null) {
				log.error("[saveAndForwardMsg] group is null");
				pack.setProtoBody(new byte[0]);
				pack.setResult((byte) -1);
				return pack;
			}
			groupMsg.setMsgSeqId(group.getSeq());
			log.info("[consultMsg] senderName=[{}]", groupMsg.getSenderName());
			msgDao.saveMsg(appId, groupMsg);
			// 转发消息；异步
			ThreadPool pool = ThreadPool.getInstance();
			SendConsultMsgWorker worker = new SendConsultMsgWorker(msg, groupMsg, customerId, shopId, consultMsgExtra);
			pool.addTask(worker);
			int capacity = pool.getRemainingCapacity();
			log.info("[consultMsg] capacity=[{}]", capacity);
			// 封装返回包
			ConsultMsg retConsultMsg = new ConsultMsg();
			retConsultMsg.setCustomerId(customerId);
			retConsultMsg.setExtra(consultMsg.getExtra());
			retConsultMsg.setShopId(shopId);
			retConsultMsg.setGroupMsg(groupMsg);
			ConsultImMsg retConsultImMsg = PbGroupMsgTools.ConsultMsg2Pb(retConsultMsg);
			pack.setProtoBody(retConsultImMsg.toByteArray());
		} catch (Exception e) {
			log.error("[consultMsg] cause is:", e);
			pack.setProtoBody(new byte[0]);
			pack.setResult((byte) -1);
		}

		return pack;
	}
	
	
	
}
