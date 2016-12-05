package cn.com.gome.logic.model.notice;

import java.io.Serializable;

/**
 * 撤回消息通知其他相关用户
 * 为防止与ProtoIM文中的类同名，在后面增加Modle作区分
 */
public class IssueRevokeMsgModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private long uid; // 用户id
	private String groupId; // 群组Id
	private String msgId; // 消息id
	private long optTime; // 操作时间
	private String extra; // 扩展

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public long getOptTime() {
		return optTime;
	}

	public void setOptTime(long optTime) {
		this.optTime = optTime;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}
}
