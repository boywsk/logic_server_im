package cn.com.gome.logic.model;

import java.io.Serializable;

import cn.com.gome.logic.protobuf.ProtocolPackage;

/**
 * 、提醒、通知信息
 */
public class MQMsg implements Serializable {
	private static final long serialVersionUID = 1L;

	protected String appId; // 应用id
	protected short cmd; // 命令字
	private boolean isPersist; // 是否持久化
	private boolean containSelf; // 消息是否包含自己
	private boolean isMsgBlocked; // 是否 apns push
	private GroupMsg groupMsg; // 聊天消息
	private NoticeMsgModel noticeMsgModel; // 功能性消息
	private ProtocolPackage pack; //协议报文封装类

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public short getCmd() {
		return cmd;
	}

	public void setCmd(short cmd) {
		this.cmd = cmd;
	}

	public boolean isPersist() {
		return isPersist;
	}

	public void setPersist(boolean isPersist) {
		this.isPersist = isPersist;
	}

	public boolean getContainSelf() {
		return containSelf;
	}

	public void setContainSelf(boolean containSelf) {
		this.containSelf = containSelf;
	}

	public GroupMsg getGroupMsg() {
		return groupMsg;
	}

	public void setGroupMsg(GroupMsg groupMsg) {
		this.groupMsg = groupMsg;
	}

	public boolean isMsgBlocked() {
		return isMsgBlocked;
	}

	public void setMsgBlocked(boolean isMsgBlocked) {
		this.isMsgBlocked = isMsgBlocked;
	}

	public NoticeMsgModel getNoticeMsgModel() {
		return noticeMsgModel;
	}

	public void setNoticeMsgModel(NoticeMsgModel noticeMsgModel) {
		this.noticeMsgModel = noticeMsgModel;
	}

	public ProtocolPackage getPack() {
		return pack;
	}

	public void setPack(ProtocolPackage pack) {
		this.pack = pack;
	}

}
