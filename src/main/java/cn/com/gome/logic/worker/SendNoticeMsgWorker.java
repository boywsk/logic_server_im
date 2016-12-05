package cn.com.gome.logic.worker;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;
import cn.com.gome.logic.dao.NoticeMsgDao;
import cn.com.gome.logic.dao.UserRedisDao;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.global.Constant.NOTICEMSG_TYPE;
import cn.com.gome.logic.global.Constant.QUIT_TYPE;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.model.NoticeMsgModel;
import cn.com.gome.logic.model.notice.IssueRevokeMsgModel;
import cn.com.gome.logic.model.notice.QuitGroupMsgModel;
import cn.com.gome.logic.model.notice.SaveNoticeMsg;
import cn.com.gome.logic.mq.MQSender;
import cn.com.gome.logic.pbTools.NoticeMsgTools;
import cn.com.gome.logic.pbTools.PackageTools;
import cn.com.gome.logic.protobuf.ProtoIM.NoticeMsg;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.utils.IPUtils;
import cn.com.gome.logic.utils.JedisUtils;
import cn.com.gome.logic.utils.ProtocolPackageUtils;
import cn.com.gome.logic.utils.TcpDataSendUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import redis.clients.jedis.JedisCluster;

public class SendNoticeMsgWorker implements Runnable {
	private static Logger log = LoggerFactory.getLogger(SendNoticeMsgWorker.class);

	private NoticeMsgModel noticeMsgModel;
	private int traceId;
	private String appId;
	private short cmd;

	public SendNoticeMsgWorker(NoticeMsgModel noticeMsgModel, int traceId, String appId, short cmd) {
		this.noticeMsgModel = noticeMsgModel;
		this.traceId = traceId;
		this.appId = appId;
		this.cmd = cmd;
	}

	@Override
	public void run() {
		int noticeType = noticeMsgModel.getNoticeType();
		String msgId = noticeMsgModel.getMsgId();
		log.info("[SendNoticeMsgWorker] traceId=[{}], appId=[{}], cmd=[{}], noticeType=[{}], msgId=[{}]",
				traceId, appId, cmd, noticeType, msgId);
		if(noticeType == NOTICEMSG_TYPE.QUITGROUPMSG.value){// 退/踢出群
			if(noticeMsgModel.getQuitGroup() != null){
				quitGroup(msgId, appId, cmd, noticeType, noticeMsgModel.getQuitGroup(), traceId);
			}else{
				log.error("[SendNoticeMsgWorker] traceId=[{}], Object QuitGroupMsgModel is NULL!", traceId);
			}
		}else if(noticeType == NOTICEMSG_TYPE.ISSUEREVOKEMSG.value){// 撤销消息通知消息
			if(noticeMsgModel.getIssueRevoke() != null){
				issueRevoke(msgId, appId, cmd, noticeType, noticeMsgModel.getIssueRevoke(), traceId);
			}else{
				log.error("[SendNoticeMsgWorker] traceId=[{}], Object IssueRevokeMsgModel is NULL!", traceId);
			}
		}else{
			log.error("[SendNoticeMsgWorker] traceId=[{}], Error NoticeType!", traceId);
		}
	}
	
	/**
	 * 退/踢出群
	 */
	public void quitGroup(String msgId, String appId, short cmd, int noticeType, QuitGroupMsgModel quitGroupMsgModel,
			int traceId) {
		long fromUid = quitGroupMsgModel.getFromUid();
		String groupId = quitGroupMsgModel.getGroupId();
		NoticeMsgDao noticeMsgDao = new NoticeMsgDao();
		long gorupUserId = noticeMsgDao.getToUid(appId, groupId);// 根据groupId获取群主userId
		if (quitGroupMsgModel.getQuitType() == QUIT_TYPE.PASSIVE.value) {// 被踢
			JedisCluster jedis = JedisUtils.getInstance().getJedisCluster();
			String idsKey = appId + "_" + groupId + Constant.REDIS_GROUP_MEMBER_SUFFIX;
			Map<String, String> idMap = jedis.hgetAll(idsKey);//获取群成员列表
			if (idMap == null) {
				log.info("成员列表为空");
			}
			for (String id : idMap.keySet()) {//群主通知所有未被踢出群的群成员，XXX（，XXX……）被踢出群聊。
				long toUid = Long.valueOf(id);
				SaveNoticeMsg saveNoticeMsg = new SaveNoticeMsg();
				saveNoticeMsg.setMsgId(msgId);
				saveNoticeMsg.setFromUid(gorupUserId);
				saveNoticeMsg.setGroupId(groupId);
				saveNoticeMsg.setToUid(toUid);
				saveNoticeMsg.setNoticeType(noticeType);
				saveNoticeMsg.setNoticeMsgJson(JSON.toJSONString(quitGroupMsgModel));
				sendNoticeMsg(traceId, appId, cmd, saveNoticeMsg, noticeType);
			}
			List<Long> kickedUids = quitGroupMsgModel.getKickedUids();
			for (int i = 0; i < kickedUids.size(); i++) {//通知被踢出的群成员，您已被群主请群聊。
				SaveNoticeMsg saveNoticeMsg = new SaveNoticeMsg();
				saveNoticeMsg.setMsgId(msgId);
				saveNoticeMsg.setFromUid(gorupUserId);
				saveNoticeMsg.setGroupId(groupId);
				saveNoticeMsg.setToUid(kickedUids.get(i));
				saveNoticeMsg.setNoticeType(noticeType);
				saveNoticeMsg.setNoticeMsgJson(JSON.toJSONString(quitGroupMsgModel));
				sendNoticeMsg(traceId, appId, cmd, saveNoticeMsg, noticeType);
			}
		}
		if (quitGroupMsgModel.getQuitType() == QUIT_TYPE.INITIATIVE.value) {// 主动退出
			JedisCluster jedis = JedisUtils.getInstance().getJedisCluster();
			String idsKey = appId + "_" + groupId + Constant.REDIS_GROUP_MEMBER_SUFFIX;
			Map<String, String> idMap = jedis.hgetAll(idsKey);
			if (idMap == null) {
				log.info("成员列表为空");
			}
			for (String id : idMap.keySet()) {
				long toUid = Long.valueOf(id);
				/**
				 * 主动退出，给所在成员发通知（不给自己发送），之前的逻辑
				 * 现在逻辑：支持多终端，不考虑是否为当前平台，每个终端都给发通知。
				 * 1.客户端对操作的当前平台直接退出并做相应的操作，收到通知后可忽略不处理（例：IOS端）
				 * 2.非当前端，收到通知后，做相应的操作。
				if (toUid == fromUid) {
					continue;
				}
				*/
				SaveNoticeMsg saveNoticeMsg = new SaveNoticeMsg();
				saveNoticeMsg.setMsgId(msgId);
				saveNoticeMsg.setFromUid(fromUid);
				saveNoticeMsg.setGroupId(groupId);
				saveNoticeMsg.setToUid(toUid);
				saveNoticeMsg.setNoticeType(noticeType);
				saveNoticeMsg.setNoticeMsgJson(JSON.toJSONString(quitGroupMsgModel));
				sendNoticeMsg(traceId, appId, cmd, saveNoticeMsg, noticeType);
			}
		}
	}
	
	/**
	 * 撤销消息通知
	 */
	public void issueRevoke(String msgId, String appId, short cmd, int noticeType, IssueRevokeMsgModel issueRevokeMsgModel,
			int traceId) {
		SaveNoticeMsg saveNoticeMsg = new SaveNoticeMsg();
		long uid = issueRevokeMsgModel.getUid();
		String groupId = issueRevokeMsgModel.getGroupId();
		JedisCluster jedis = JedisUtils.getInstance().getJedisCluster();
		String idsKey = appId + "_" + groupId + Constant.REDIS_GROUP_MEMBER_SUFFIX;
		Map<String, String> idMap = jedis.hgetAll(idsKey);
		for (String id : idMap.keySet()) {
			long toUid = Long.parseLong(id);
			saveNoticeMsg.setMsgId(msgId);
			saveNoticeMsg.setFromUid(uid);
			saveNoticeMsg.setToUid(toUid);
			saveNoticeMsg.setGroupId(groupId);
			saveNoticeMsg.setNoticeType(noticeType);
			saveNoticeMsg.setNoticeMsgJson(JSON.toJSONString(issueRevokeMsgModel));
			sendNoticeMsg(traceId, appId, cmd, saveNoticeMsg, noticeType);
		}
		//步骤2--修改消息状态
//		NoticeMsgDao noticeMsgDao = new NoticeMsgDao();
//		noticeMsgDao.ModifyMsgStatusRevoke(appId, groupId, msgId, traceId);
	}
	
	/**
	 * 发送消息
	 */
	private void sendNoticeMsg(int traceId, String appId, short cmd, SaveNoticeMsg saveNoticeMsg, int noticeType) {
		long fromUid = saveNoticeMsg.getFromUid();
		long toUid = saveNoticeMsg.getToUid();
		log.info("[sendNoticeMsg] tranceId=[{}],fromUid=[{}],NoticeType=[{}],CMD=[{}]", traceId, fromUid, noticeType, cmd);
		try {
			UserRedisDao userRedisDao = new UserRedisDao();
			// 获取登录平台信息
			String platform = userRedisDao.getPlatform(appId, toUid);
			if (!StringUtils.isBlank(platform)) {
				saveNoticeMsg.setPlatform(Integer.parseInt(platform));
			} else {
				saveNoticeMsg.setPlatform(1);// 默认为1（移动端）
			}
			saveNoticeMsg.setSendTime(System.currentTimeMillis());
			NoticeMsgDao noticeMsgDao = new NoticeMsgDao();
			noticeMsgDao.saveNoticeMsg(saveNoticeMsg, appId, toUid, traceId);// 保存消息到数据库中
			log.info("[SendNoticeMsg] traceId={}],ToUid=[{}] save NoticeMsg complete!", traceId, toUid);
			Map<String, String> map = userRedisDao.listUserRsp(appId, toUid);
			if (map != null && !map.isEmpty()) {
				for (String key : map.keySet()) {
					String[] keyArr = key.split("_");
					if (keyArr.length < 2) {
						continue;
					}
					byte bClientId = Integer.valueOf(keyArr[0]).byteValue();
					log.info("[SendNoticeMsg] tranceId=[{}], Notice toUuid=[{}],key=[{}],msgId=[{}]", traceId, toUid,
							key, saveNoticeMsg.getMsgId());
					String clientKey = map.get(key);
					String[] rspArr = clientKey.split(":");
					if (rspArr.length >= 2) {
						long rspIp = Long.valueOf(rspArr[0]);
						int rspport = Integer.parseInt(rspArr[1]);
						log.info("[SendNoticeMsg] tranceId=[{}], Notice toUid=[{}],rspIp=[{}],rspport=[{}]", traceId,
								toUid, IPUtils.longToIP(rspIp), rspport);
						ProtocolPackage pack = getPackOnline(rspIp, rspport, toUid, bClientId, saveNoticeMsg);
						TcpDataSendUtils.sendTcpData(clientKey, pack);
					}
				}
			} else {// 不在线
				log.info("[SendNoticeMsg]==push==tranceId=[{}],fromUid=[{}],ToUid=[{}],NoticyType=[{}]", traceId,
						fromUid, toUid, noticeType);
				ProtocolPackage pack = getPackOffline(toUid, saveNoticeMsg);
				byte[] buf = ProtocolPackageUtils.package2Byte(pack);
				MQSender.getInstance().sendMsg(buf);
			}
		} catch (Exception e) {
			log.error("[run] cause is:", e);
		}
	}
	/**
	 * 获取Pack,online
	 */
	private ProtocolPackage getPackOnline(long rspIp, int rspport, long toUid, byte bClientId,
			SaveNoticeMsg saveNoticeMsg) {
		ProtocolPackage pack = new ProtocolPackage();
		pack.setTraceId(traceId);
		pack.setAppId(appId);
		NoticeMsg pbMsg = NoticeMsgTools.notice2PbMsg(saveNoticeMsg, traceId);
		byte[] protoBody = pbMsg.toByteArray();
		byte[] head = PackageTools.generateHead(appId, saveNoticeMsg.getFromUid(), cmd);
		pack.setHead(head);
		pack.setProtoBody(protoBody);
		pack.setAck(Constant.PACK_ACK.NO.value);
		pack.setRspIP(rspIp);
		pack.setClientId(bClientId);
		pack.setRspPort(rspport);
		pack.setReceiveId(toUid);
		return pack;
	}
	/**
	 * 获取Pack,offline
	 */
	private ProtocolPackage getPackOffline(long toUid, SaveNoticeMsg saveNoticeMsg) {
		ProtocolPackage pack = new ProtocolPackage();
		pack.setStartTag(Global.HEAD_START_TAG);
		pack.setTraceId(traceId);
		pack.setAppId(appId);
		pack.setReceiveId(saveNoticeMsg.getToUid());
		NoticeMsg pbMsg = NoticeMsgTools.notice2PbMsg(saveNoticeMsg, traceId);
		byte[] protoBody = pbMsg.toByteArray();
		long uid = saveNoticeMsg.getFromUid();
		long receiveId = saveNoticeMsg.getToUid();
		short length = (short)(Global.PACK_HEAD_LENGTH + 2 + protoBody.length);
		
		ByteBuf head = Unpooled.buffer(Global.PACK_HEAD_LENGTH + 2);
		head.writeShort(length);// 包头+包体总长
		head.writeShort(cmd);// 命令字
		head.writeLong(uid);// userId
		head.writeByte(0);// 10:ios/11:android/12:wp/20:pc/21:mac/22:ubuntu/23:linux/24:unix/25:ipad/30:Web/40:H5
		head.writeByte(0);// 协议版本号
		head.writeByte(0);// 客户端类型;0:IM/1:push
		head.writeByte(0);// response时的error code
		head.writeLong(System.currentTimeMillis());// 服务器端时间
		head.writeLong(receiveId);// 消息接受者
		head.writeLong(0L);// gateWay ip
		head.writeInt(0);// gateWay port
		head.writeLong(traceId);// 染色id/跟踪id
		head.writeByte(Constant.PACK_ACK.NO.value);//回包标记(是否是请求应答包)；0:否、1:是
		ByteBuf appIdBuf = Unpooled.buffer(Global.APPID_LENGTH);
		try {
			appIdBuf.writeBytes(appId.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			log.error("[generatePack]:", e);
		}
		head.writeBytes(appIdBuf);
		head.writeByte(1);//报文超长时分包，分包是否是最后一个包；0:否、1:是
		head.writeBytes(new byte[14]);// 补充 byte[14]
		
		pack.setHead(head.array());
		pack.setProtoBody(protoBody);
		pack.setEndTag(Global.HEAD_END_TAG);
		return pack;
	}

}
