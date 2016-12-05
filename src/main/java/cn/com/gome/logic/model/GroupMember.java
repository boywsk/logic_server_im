package cn.com.gome.logic.model;

import java.io.Serializable;

/**
 * 群成员
 */
public class GroupMember implements Serializable {
	private static final long serialVersionUID = 1L;

	private String groupId; // 群组
	private long uid; // 成员id
	private long initSeq;// 加入群时，当前群消息seq
	private long readSeq;// 读取到的群消息最大seq
	private long joinTime; // 加入时间
	private long updateTime;// 最后一次修改时间
	private int isMsgBlocked; //是否免打扰；0:否,1:是
	
//	private int groupIdHash;
//	private long userId;
//	private int identity;// 身份;0:普通成员,1:创建者,2:管理员
//	private int isTop; // 置顶 0:否 1:是
//	private int isShield; // 屏蔽群消息 0:否 1:是
//	private int status;// 0:未通过 1:通过 2:拒绝
//	private long maxSeq; // 成员群seq
//	private long receiveSeqId;//客户端接收到聊天消息最大seqId
//	private int stickies; // 置顶;0:否,1:是

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public long getInitSeq() {
		return initSeq;
	}

	public void setInitSeq(long initSeq) {
		this.initSeq = initSeq;
	}

	public long getReadSeq() {
		return readSeq;
	}

	public void setReadSeq(long readSeq) {
		this.readSeq = readSeq;
	}

	public long getJoinTime() {
		return joinTime;
	}

	public void setJoinTime(long joinTime) {
		this.joinTime = joinTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public int getIsMsgBlocked() {
		return isMsgBlocked;
	}

	public void setIsMsgBlocked(int isMsgBlocked) {
		this.isMsgBlocked = isMsgBlocked;
	}
}
