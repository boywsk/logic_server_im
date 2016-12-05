package cn.com.gome.logic.pbTools;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.model.Attachment;
import cn.com.gome.logic.model.ConsultMsg;
import cn.com.gome.logic.model.Group;
import cn.com.gome.logic.model.GroupMember;
import cn.com.gome.logic.model.GroupMsg;
import cn.com.gome.logic.model.MsgLocation;
import cn.com.gome.logic.protobuf.ProtoIM.ConsultImMsg;
import cn.com.gome.logic.protobuf.ProtoIM.ImGroup;
import cn.com.gome.logic.protobuf.ProtoIM.ImMsg;
import cn.com.gome.logic.protobuf.ProtoIM.ImMsgAttach;
import cn.com.gome.logic.protobuf.ProtoIM.ImMsgLocation;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;

/**
 * 群组消息对象和pb对象互转工具
 */
public class PbGroupMsgTools {
	static Logger log = LoggerFactory.getLogger(PbGroupMsgTools.class);
	
	/**
	 * 聊天消息转pb格式
	 * @param msg
	 * @return
	 */
	public static ImMsg groupMsg2PbImMsg(GroupMsg msg) {
		ImMsg.Builder pbImMsg = ImMsg.newBuilder();
		if(msg == null) {
			return pbImMsg.build();
		}
		pbImMsg.setMsgId(msg.getMsgId());
		if(msg.getMsgType() > 0) {
			pbImMsg.setMsgType(msg.getMsgType());
		}
		String msgBody = msg.getMsgBody();
		if(!Strings.isNullOrEmpty(msgBody)) {
			pbImMsg.setMsgBody(msgBody);
		}
//		pbImMsg.setSponsorId(msg.getSponsorId());
//		pbImMsg.setSenderType(msg.getSenderType());
		pbImMsg.setSenderId(msg.getSenderId());
		String senderName = msg.getSenderName();
		if(!Strings.isNullOrEmpty(senderName)) {
			pbImMsg.setSenderName(senderName);
		}
		String senderRemark = msg.getSenderRemark();
		if(!Strings.isNullOrEmpty(senderRemark)) {
			pbImMsg.setSenderRemark(senderRemark);
		}
		String groupId = msg.getGroupId();
		if(!Strings.isNullOrEmpty(groupId)) {
			pbImMsg.setGroupId(groupId);
		}
		int groupType = msg.getGroupType();
		if(groupType > 0) {
			pbImMsg.setGroupType(msg.getGroupType());
		}
		String groupName = msg.getGroupName();
		if(!Strings.isNullOrEmpty(groupName)) {
			pbImMsg.setGroupName(groupName);
		}
		long sendTime = msg.getSendTime();
		if(sendTime > 0) {
			pbImMsg.setSendTime(sendTime);
		}
		pbImMsg.setMsgSeqId(msg.getMsgSeqId());
		String msgUrl = msg.getMsgUrl();
		if(!Strings.isNullOrEmpty(msgUrl)) {
			pbImMsg.setMsgUrl(msgUrl);
		}
		List<Attachment> attachs = msg.getMsgAttch();
		if(attachs != null) {
			for(Attachment attach : attachs) {
				ImMsgAttach pbAttach = ImMsgAttach2PbAttach(attach);
				pbImMsg.addAttch(pbAttach);
			}
		}
		boolean origiImg = msg.isOrigiImg();
		pbImMsg.setOrigiImg(origiImg);
		MsgLocation location = msg.getMsgLocation();
		if(location != null) {
			ImMsgLocation pbLocaltion =  PbMsgTools.location2PbLocation(location);
			pbImMsg.setLocation(pbLocaltion);
		}
		String extra = msg.getExtra();
		if(!Strings.isNullOrEmpty(extra)) {
			pbImMsg.setExtra(extra);
		}
		List<Long> uids = msg.getAtUids();
		if(uids != null) {
			for(long uid : uids) {
				pbImMsg.addAtUids(uid);
			}
		}
		int pushStatus = msg.getPushStatus();//消息推送状态，0:推送、1:不推送
		pbImMsg.setPushStatus(pushStatus);
		int msgStatus = msg.getMsgStatus();//消息状态，0:正常、1:撤回、2:删除
		pbImMsg.setMsgStatus(msgStatus);
		List<Long> delUids = msg.getMsgDelUids();//删除消息该条消息的用户id列表
		if(delUids != null){
			for(long uid : uids){
				pbImMsg.addMsgDelUids(uid);
			}
		}
		return pbImMsg.build();
	}
	
	/**
	 * pb格式转聊天消息
	 * @param pbImMsg
	 * @return
	 */
	public static GroupMsg PbImMsg2GroupMsg(ImMsg pbImMsg) {
		GroupMsg msg = new GroupMsg();
		if(pbImMsg == null) {
			return msg;
		}
		String msgId = pbImMsg.getMsgId();
		msg.setMsgId(msgId);
		int msgType = pbImMsg.getMsgType();
		msg.setMsgType(msgType);
		if(pbImMsg.hasMsgBody()) {
			String msgBody = pbImMsg.getMsgBody();
			msg.setMsgBody(msgBody);
		}
//		msg.setSponsorId(pbImMsg.getSponsorId());
//		msg.setSenderType(pbImMsg.getSenderType());
		long senderId = pbImMsg.getSenderId();
		msg.setSenderId(senderId);
		if(pbImMsg.hasSenderName()) {
			String senderName = pbImMsg.getSenderName();
			msg.setSenderName(senderName);
		}
		if(pbImMsg.hasSenderRemark() && pbImMsg.getSenderRemark().length() > 0) {
			msg.setSenderRemark(pbImMsg.getSenderRemark());
		}
		//log.info("groupMsg senderRemark=[{}]", msg.getSenderRemark());
		String groupId = pbImMsg.getGroupId();
		msg.setGroupId(groupId);
		int groupType = pbImMsg.getGroupType();
		msg.setGroupType(groupType);
		if(pbImMsg.hasGroupName()) {
			String groupName = pbImMsg.getGroupName();
			msg.setGroupName(groupName);
		}
		msg.setSendTime(msg.getSendTime());
		long msgSeqId = pbImMsg.getMsgSeqId();
		msg.setMsgSeqId(msgSeqId);
		if(pbImMsg.hasMsgUrl() && pbImMsg.getMsgUrl().length() > 0) {
			String msgUrl = pbImMsg.getMsgUrl();
			msg.setMsgUrl(msgUrl);
		}
		if(pbImMsg.getAttchCount() > 0) {
			List<ImMsgAttach> pbAttachList = pbImMsg.getAttchList();
			List<Attachment> list = new ArrayList<Attachment>();
			if(pbAttachList != null) {
				for(ImMsgAttach msgAttach : pbAttachList) {
					Attachment attach = PbAttach2Attachment(msgAttach);
					if(attach != null) {
						list.add(attach);
					}
				}
			}
			msg.setMsgAttch(list);
		}
		boolean origiImg = pbImMsg.getOrigiImg();
		msg.setOrigiImg(origiImg);
		if(pbImMsg.hasLocation()) {
			MsgLocation location = PbMsgTools.pbLocation2Location(pbImMsg.getLocation());
			msg.setMsgLocation(location);
		}
		if(pbImMsg.hasExtra()) {
			String extra = pbImMsg.getExtra();
			msg.setExtra(extra);
		}
		if(pbImMsg.getAtUidsCount() > 0) {
			List<Long> uids = pbImMsg.getAtUidsList();
			if(uids != null) {
				msg.setAtUids(uids);
			}
		}
		int pushStatus = pbImMsg.getPushStatus();//消息推送状态，0:推送、1:不推送
		msg.setPushStatus(pushStatus);
		int msgStatus = pbImMsg.getMsgStatus();//消息状态，0:正常、1:撤回、2:删除
		msg.setMsgStatus(msgStatus);
		if(pbImMsg.getMsgDelUidsCount() > 0){
			List<Long> delUids = pbImMsg.getMsgDelUidsList();//删除消息该条消息的用户id列表
			if(delUids != null){
				msg.setMsgDelUids(delUids);
			}
		}
		return msg;
	}
	
	/**
	 * 附件转pb格式
	 * @param attach
	 * @return
	 */
	public static ImMsgAttach ImMsgAttach2PbAttach(Attachment attach) {
		ImMsgAttach.Builder pbAttach = ImMsgAttach.newBuilder();
		if(attach == null) {
			return pbAttach.build();
		}
		String id = attach.getId();
		if(!Strings.isNullOrEmpty(id)) {
			pbAttach.setAttachId(id);
		}
		String name = attach.getAttachName();
		if(!Strings.isNullOrEmpty(name)) {
			pbAttach.setAttachName(name);
		}
		pbAttach.setAttachType(attach.getAttachType());
		String attachUrl = attach.getAttachUrl();
		if(!Strings.isNullOrEmpty(attachUrl)) {
			pbAttach.setAttachUrl(attachUrl);
		}
		pbAttach.setAttachSize(attach.getAttachSize());
		pbAttach.setWidth(attach.getWidth());
		pbAttach.setHeight(attach.getHeight());
		pbAttach.setAttachPlaytime(attach.getAttachPlaytime());
		pbAttach.setAttachUploadtime(attach.getAttachUploadtime());
		String extra = attach.getExtra();
		if(!Strings.isNullOrEmpty(extra)) {
			pbAttach.setExtra(extra);
		}
		
		return pbAttach.build();
	}
	
	/**
	 * 附件pb格式转对象
	 * @param pbAttach
	 * @return
	 */
	public static Attachment PbAttach2Attachment(ImMsgAttach pbAttach) {
		Attachment attach = new Attachment();
		if(pbAttach == null) {
			return attach;
		}
		String id = pbAttach.getAttachId();
		attach.setId(id);
		String attachName = pbAttach.getAttachName();
		attach.setAttachName(attachName);
		int attachType = pbAttach.getAttachType();
		attach.setAttachType(attachType);
		String attachUrl = pbAttach.getAttachUrl();
		attach.setAttachUrl(attachUrl);
		int attachSize = pbAttach.getAttachSize();
		attach.setAttachSize(attachSize);
		int width = pbAttach.getWidth();
		attach.setWidth(width);
		int Height = pbAttach.getHeight();
		attach.setHeight(Height);
		int attachPlaytime = pbAttach.getAttachPlaytime();
		attach.setAttachPlaytime(attachPlaytime);
		long attachUploadtime = pbAttach.getAttachUploadtime();
		attach.setAttachUploadtime(attachUploadtime);
		if(pbAttach.hasExtra()) {
			String extra = pbAttach.getExtra();
			attach.setExtra(extra);
		}
		
		return attach;
	}
	
	/**
	 * 群组对象转pb格式
	 * @param group
	 * @param member
	 * @return
	 */
	public static ImGroup grou2PbGroup(Group group, GroupMember member) {
		if(group == null) {
			return null;
		}
		ImGroup.Builder pbGroup = ImGroup.newBuilder();
		pbGroup.setGroupId(group.getGroupId());
		int type = group.getType();
		pbGroup.setGroupType(type);
		long seqId = group.getSeq();
		pbGroup.setSeqId(seqId);
		long initSeq = 0;
		long readSeq = 0;
		boolean isMsgBlocked = false;
		if(member != null) {
			initSeq = member.getInitSeq(); 
			readSeq = member.getReadSeq();
			if(member.getIsMsgBlocked() == 1) {
				isMsgBlocked = true;
			}
		}
		pbGroup.setInitSeqId(initSeq);
		pbGroup.setReadSeqId(readSeq);
		pbGroup.setIsMsgBlocked(isMsgBlocked);
		String msgJson = group.getLastMsg();
		//log.info("msgJson=[{}]", msgJson);
		if(msgJson != null) {
			GroupMsg msg = JSON.parseObject(msgJson, GroupMsg.class);
			ImMsg pbMsg = groupMsg2PbImMsg(msg);
			pbGroup.setLastMsg(pbMsg);
		}
		return pbGroup.build();
	}
	
	/**
	 * 群组对象转pb格式
	 * @param groupId
	 * @return
	 */
	public static ImGroup grou2PbGroup(int groupType, String groupId) {
		ImGroup.Builder pbGroup = ImGroup.newBuilder();
		pbGroup.setGroupId(groupId);
		pbGroup.setGroupType(groupType);
		pbGroup.setIsQuit(true);
		return pbGroup.build();
	}
	
	/**
	 * 客服消息转pb
	 * @param msg
	 * @return
	 */
	public static ConsultImMsg ConsultMsg2Pb(ConsultMsg msg) {
		ConsultImMsg.Builder pbConsultMsg = ConsultImMsg.newBuilder();
		pbConsultMsg.setCustomerId(msg.getCustomerId());
		GroupMsg groupMsg = msg.getGroupMsg();
		ImMsg pbImMsg = groupMsg2PbImMsg(groupMsg);
		pbConsultMsg.setImMsg(pbImMsg);
		String extra = msg.getExtra();
		if(!Strings.isNullOrEmpty(extra)) {
			pbConsultMsg.setExtra(extra);
		}
		String shopId = msg.getShopId();
		if(!Strings.isNullOrEmpty(shopId)) {
			pbConsultMsg.setShopId(shopId);
		}
		
		return pbConsultMsg.build();
	}
	
	/**
	 * pb转客服消息
	 * @param pbMsg
	 * @return
	 */
	public static ConsultMsg pb2ConsultMsg(ConsultImMsg pbMsg) {
		ConsultMsg consultMsg = new ConsultMsg();
		consultMsg.setCustomerId(pbMsg.getCustomerId());
		ImMsg pbImMsg = pbMsg.getImMsg();
		GroupMsg groupMsg = PbImMsg2GroupMsg(pbImMsg);
		consultMsg.setGroupMsg(groupMsg);
		if(pbMsg.hasExtra()) {
			String extra = pbMsg.getExtra();
			consultMsg.setExtra(extra);
		}
		if(pbMsg.hasShopId()) {
			String shopId = pbMsg.getShopId();
			consultMsg.setShopId(shopId);
		}
		
		return consultMsg;
	}
}
