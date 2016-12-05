package cn.com.gome.logic.model.notice;

import java.io.Serializable;

public class SaveNoticeMsg implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String msgId;
	private long fromUid;
	private long toUid;
	private String groupId;
	private int noticeType;
	private String NoticeMsgJson;
	private long sendTime;
	private int platform;

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public long getFromUid() {
		return fromUid;
	}

	public void setFromUid(long fromUid) {
		this.fromUid = fromUid;
	}

	public long getToUid() {
		return toUid;
	}

	public void setToUid(long toUid) {
		this.toUid = toUid;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public int getNoticeType() {
		return noticeType;
	}

	public void setNoticeType(int noticeType) {
		this.noticeType = noticeType;
	}

	public String getNoticeMsgJson() {
		return NoticeMsgJson;
	}

	public void setNoticeMsgJson(String noticeMsgJson) {
		NoticeMsgJson = noticeMsgJson;
	}

	public long getSendTime() {
		return sendTime;
	}

	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}

	public int getPlatform() {
		return platform;
	}

	public void setPlatform(int platform) {
		this.platform = platform;
	}

}
