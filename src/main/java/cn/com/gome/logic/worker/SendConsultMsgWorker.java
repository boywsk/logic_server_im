package cn.com.gome.logic.worker;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import cn.com.gome.logic.dao.UserRedisDao;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.model.ConsultMsg;
import cn.com.gome.logic.model.GroupMsg;
import cn.com.gome.logic.mq.ConsultMQSender;
import cn.com.gome.logic.mq.MQSender;
import cn.com.gome.logic.pbTools.PbGroupMsgTools;
import cn.com.gome.logic.protobuf.ProtoIM.ConsultImMsg;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.utils.IPUtils;
import cn.com.gome.logic.utils.ProtocolPackageUtils;
import cn.com.gome.logic.utils.TcpDataSendUtils;

/**
 * 客服消息发送工作线程
 */
public class SendConsultMsgWorker implements Runnable {
	 Logger log = LoggerFactory.getLogger(SendConsultMsgWorker.class);
	 
	private ProtocolPackage pack;
	private GroupMsg groupMsg;
	private long customerId;
	private String shopId;
	private String consultMsgExtra;
	
	public SendConsultMsgWorker(ProtocolPackage pack, GroupMsg groupMsg, 
			long customerId, String shopId, String consultMsgExtra) {
		this.customerId = customerId;
		this.groupMsg = groupMsg;
		this.pack = pack;
		this.shopId = shopId;
		this.consultMsgExtra = consultMsgExtra;
	}

	@Override
	public void run() {
		byte clientId = pack.getClientId();
		try {
			String appId = pack.getAppId().trim();
//			long senderUid = groupMsg.getSenderId();
			String groupId = groupMsg.getGroupId();
			long vUid = pack.getUid();
			log.info("[run] appId=[{}],customerId=[{}],vUid=[{}],groupId=[{}],clientId=[{}],msgSeqId=[{}]", 
					appId, customerId, vUid, groupId, clientId, groupMsg.getMsgSeqId());
			String[] groupIdArr = groupId.split("_");
			if(groupIdArr.length < 2) {
				log.error("[run] groupId error!!! appId=[{}],customerId=[{}],vUid=[{}],groupId=[{}],clientId=[{}]", 
						appId, customerId, vUid, groupId, clientId);
				return;
			}
			
			String msgId = groupMsg.getMsgId();
			long fromUid = groupMsg.getSenderId();
			int groupType = groupMsg.getGroupType();
			int msgType =  groupMsg.getMsgType();
			long currTime = System.currentTimeMillis();
			log.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", "σσmessage", msgId, 
					fromUid, 0, appId, groupType, msgType, 0, groupId, currTime);
			UserRedisDao userRedisDao = new UserRedisDao();
			//判断店铺客服类型；uid_9999999997_shopId，转发消息去除shopId
			int length = groupIdArr.length;
			if(!Strings.isNullOrEmpty(shopId) && length > 2) {
				length = 2;
			}
			
			ConsultMsg retConsultMsg = new ConsultMsg();
			retConsultMsg.setCustomerId(customerId);
			retConsultMsg.setExtra(consultMsgExtra);
			retConsultMsg.setShopId(shopId);
			retConsultMsg.setGroupMsg(groupMsg);
			ConsultImMsg retConsultImMsg = PbGroupMsgTools.ConsultMsg2Pb(retConsultMsg);
			
			//客户端用户id消息转发到接入层
			long uid = Long.parseLong(groupIdArr[0]);
			Map<String, String> map = userRedisDao.listUserRsp(appId, uid);
			if (map != null && !map.isEmpty()) {
				log.info("[run] chat uid=[{}],resp size=[{}]", uid, map.size());
				for (String key : map.keySet()) {
					String[] keyArr = key.split("_");
					if (keyArr.length < 2) {
						continue;
					}
					byte bClientId = Integer.valueOf(keyArr[0]).byteValue();
					log.info("[run] chat uid=[{}],key=[{}],bClientId=[{}]", uid, key, bClientId);
					if(vUid == uid && bClientId == clientId) {
						log.info("[run self] uid=[{}],clientId=[{}],bClientId=[{}]", uid, clientId, bClientId);
						continue;
					}
					
					ProtocolPackage msg = pack.deepCopy();
					String clientKey = map.get(key);
					String[] rspArr = clientKey.split(":");
					long rspIp = Long.valueOf(rspArr[0]);
					int rspport = Integer.parseInt(rspArr[1]);
					log.info("[run] chat uid=[{}],rspIp=[{}],rspport=[{}]", uid, IPUtils.longToIP(rspIp), rspport);
					msg.setAck(Constant.PACK_ACK.NO.value);
					msg.setRspIP(rspIp);
					msg.setRspPort(rspport);
					msg.setReceiveId(uid);
					msg.setClientId(bClientId);
					msg.setProtoBody(retConsultImMsg.toByteArray());
					TcpDataSendUtils.sendTcpData(clientKey, msg);
					
					log.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", "σσmessage", msgId, 
							fromUid, bClientId, appId, groupType, msgType, uid, groupId, currTime);
				}
			} else {// 不在线;不给客服发apns push
				if (uid != customerId) {
					log.info("[sendSingleChat]=========ios push==========vUid=[{}],uid=[{}]", vUid, uid);
					ProtocolPackage msg = pack.deepCopy();
					msg.setReceiveId(uid);
					msg.setProtoBody(retConsultImMsg.toByteArray());
					byte[] buf = ProtocolPackageUtils.package2Byte(msg);
					MQSender.getInstance().sendMsg(buf);
				}
			}
			//客服端用户id消息转发到MQ
			long customerUid = Long.parseLong(groupIdArr[1]);
			//消息发送者客服id，转发消息给(MQ)客服代理，如果是从handler中发确认消息到MQ
			if(fromUid != customerUid) {
				ProtocolPackage msg = pack.deepCopy();
				log.info("[run] send msg to MQ!!!");
				msg.setAck(Constant.PACK_ACK.NO.value);
				msg.setReceiveId(customerUid);
				msg.setProtoBody(retConsultImMsg.toByteArray());
				byte[] data = ProtocolPackageUtils.package2Byte(pack);
				ConsultMQSender.getInstance().sendMsg(data, customerUid);
			}
			
			
//			for (int i = 0; i < length; i++) {
//				long uid = Long.parseLong(groupIdArr[i]);
//				if(uid <= 0) {
//					continue;
//				}
//				Map<String, String> map = userRedisDao.listUserRsp(appId, uid);
//				if (map != null && !map.isEmpty()) {
//					log.info("[run] chat uid=[{}],resp size=[{}]", uid, map.size());
//					for (String key : map.keySet()) {
//						String[] keyArr = key.split("_");
//						if (keyArr.length < 2) {
//							continue;
//						}
//						byte bClientId = Integer.valueOf(keyArr[0]).byteValue();
//						log.info("[run] chat uid=[{}],key=[{}],bClientId=[{}]", uid, key, bClientId);
//						if(vUid == uid && bClientId == clientId) {
//							log.info("[run self] uid=[{}],clientId=[{}],bClientId=[{}]", uid, clientId, bClientId);
//							continue;
//						}
//						
//						ConsultMsg retConsultMsg = new ConsultMsg();
//						retConsultMsg.setCustomerId(customerId);
//						retConsultMsg.setExtra(consultMsgExtra);
//						retConsultMsg.setShopId(shopId);
//						retConsultMsg.setGroupMsg(groupMsg);
//						ConsultImMsg retConsultImMsg = PbGroupMsgTools.ConsultMsg2Pb(retConsultMsg);
//						
//						ProtocolPackage msg = pack.deepCopy();
//						String clientKey = map.get(key);
//						String[] rspArr = clientKey.split(":");
//						long rspIp = Long.valueOf(rspArr[0]);
//						int rspport = Integer.parseInt(rspArr[1]);
//						log.info("[run] chat uid=[{}],rspIp=[{}],rspport=[{}]", uid, IPUtils.longToIP(rspIp), rspport);
//						msg.setAck(Constant.PACK_ACK.NO.value);
//						msg.setRspIP(rspIp);
//						msg.setRspPort(rspport);
//						msg.setReceiveId(uid);
//						msg.setClientId(bClientId);
//						msg.setProtoBody(retConsultImMsg.toByteArray());
//						TcpDataSendUtils.sendTcpData(clientKey, msg);
//						
//						log.info("{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", "σσmessage", msgId, 
//								fromUid, bClientId, appId, groupType, msgType, uid, groupId, currTime);
//					}
//				} else {// 不在线;不给客服发apns push
//					if (uid != customerId) {
//						log.info("[sendSingleChat]=========ios push==========vUid=[{}],uid=[{}]", vUid, uid);
//						ProtocolPackage msg = pack.deepCopy();
//						msg.setReceiveId(uid);
//						ConsultMsg retConsultMsg = new ConsultMsg();
//						retConsultMsg.setCustomerId(customerId);
//						retConsultMsg.setExtra(consultMsgExtra);
//						retConsultMsg.setShopId(shopId);
//						retConsultMsg.setGroupMsg(groupMsg);
//						ConsultImMsg retConsultImMsg = PbGroupMsgTools.ConsultMsg2Pb(retConsultMsg);
//						msg.setProtoBody(retConsultImMsg.toByteArray());
//						byte[] buf = ProtocolPackageUtils.package2Byte(msg);
//						MQSender.getInstance().sendMsg(buf);
//					}
//				}
//			}
		} catch (Exception e) {
			log.error("[run]:", e);
		}
	}
}
