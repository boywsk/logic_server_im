package cn.com.gome.logic.model;

import java.io.Serializable;

/**
 * 群组退出成员
 */
public class GroupQuitMember implements Serializable {
	private static final long serialVersionUID = 1L;

	private long uid; // 用户id
	private String groupId; // 群组id
	private long createTime;// 创建时间
	
	public GroupQuitMember() {
		
	}
	
	public GroupQuitMember(long uid, String groupId) {
		this.uid = uid;
		this.groupId = groupId;
	}

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

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

}
