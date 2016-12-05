package cn.com.gome.logic.model;

import java.io.Serializable;

/**
 * 群组
 */
public class Group implements Serializable {
	private static final long serialVersionUID = 1L;

	private String groupId; // 群组id
	private int type; // 群组类型;1:单聊,2:群聊,3:系统消息/小秘书
	private long seq; // 递增id
	private String lastMsg; // 最后一条消息内容
	private long createTime; // 创建时间
	private int isDele;
	private long updateTime; // 最后一次修改时间
	//以下属性是与OA的Group对象的差异20161013仅增加userId
//	private String groupName;// 群组名称
//	private List<GroupMsg> msgs; // 最新20条消息
	private long userId; // 群主id
//	private String groupDesc;// 群描述
//	private String avatar; // 头像
//	private String qRcode; // 二维码
//	private int capacity;// 容量
//	private int isAudit; // 是否需要审核
	
	
	

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public String getLastMsg() {
		return lastMsg;
	}

	public void setLastMsg(String lastMsg) {
		this.lastMsg = lastMsg;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public int getIsDele() {
		return isDele;
	}

	public void setIsDele(int isDele) {
		this.isDele = isDele;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

}
