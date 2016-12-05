package cn.com.gome.logic.model;

import java.io.Serializable;
import java.util.List;
import com.alibaba.fastjson.JSON;

/**
 * 聊天消息
 */
public class GroupMsg implements Serializable {
	private static final long serialVersionUID = 1L;

	private String msgId; // 消息id
	private int msgType; // 1:文本、2:语音、3:图片、4:附件、5:分享/转发(通过url)、...
	private String msgBody; // 消息体
	private long senderId; // 发送者id
	private String senderName; // 发送者名称
	private String senderRemark;// 消息发送者在该群中的昵称
	private String groupId; // 群组id
	private int groupType; // 群组类型，1:单聊，2:群聊
	private String groupName; // 群组名称
	private long sendTime; // 发送服务器时间
	private long msgSeqId; // 自增计数
	private String msgUrl;// 消息url
	private boolean origiImg;
	private MsgLocation msgLocation; // 转发和分享链接URL
	private List<Attachment> msgAttch; // 附件列表
	private String extra; // 扩展信息
	private List<Long> atUids;// 被@的用户id列表

	// private List<> ImMsgAttach attch = 13; //附件
	private int pushStatus; // 消息推送状态，0:推送、1:不推送
	private int msgStatus; // 消息状态，0:正常、1:撤回、2:删除
	private List<Long> msgDelUids; // 删除消息该条消息的用户id列表

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public String getMsgBody() {
		return msgBody;
	}

	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}

	public long getSenderId() {
		return senderId;
	}

	public void setSenderId(long senderId) {
		this.senderId = senderId;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getSenderRemark() {
		return senderRemark;
	}

	public void setSenderRemark(String senderRemark) {
		this.senderRemark = senderRemark;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public int getGroupType() {
		return groupType;
	}

	public void setGroupType(int groupType) {
		this.groupType = groupType;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public long getSendTime() {
		return sendTime;
	}

	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}

	public long getMsgSeqId() {
		return msgSeqId;
	}

	public void setMsgSeqId(long msgSeqId) {
		this.msgSeqId = msgSeqId;
	}

	public String getMsgUrl() {
		return msgUrl;
	}

	public void setMsgUrl(String msgUrl) {
		this.msgUrl = msgUrl;
	}

	public boolean isOrigiImg() {
		return origiImg;
	}

	public void setOrigiImg(boolean origiImg) {
		this.origiImg = origiImg;
	}

	public MsgLocation getMsgLocation() {
		return msgLocation;
	}

	public void setMsgLocation(MsgLocation msgLocation) {
		this.msgLocation = msgLocation;
	}

	public List<Attachment> getMsgAttch() {
		return msgAttch;
	}

	public void setMsgAttch(List<Attachment> msgAttch) {
		this.msgAttch = msgAttch;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public String toString() {
		return JSON.toJSONString(this);
	}

	public List<Long> getAtUids() {
		return atUids;
	}

	public void setAtUids(List<Long> atUids) {
		this.atUids = atUids;
	}

	public int getPushStatus() {
		return pushStatus;
	}

	public void setPushStatus(int pushStatus) {
		this.pushStatus = pushStatus;
	}

	public int getMsgStatus() {
		return msgStatus;
	}

	public void setMsgStatus(int msgStatus) {
		this.msgStatus = msgStatus;
	}

	public List<Long> getMsgDelUids() {
		return msgDelUids;
	}

	public void setMsgDelUids(List<Long> msgDelUids) {
		this.msgDelUids = msgDelUids;
	}
}
