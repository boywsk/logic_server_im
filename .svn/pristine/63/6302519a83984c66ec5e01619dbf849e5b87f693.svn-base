package cn.com.gome.logic.model;

import java.io.Serializable;

import cn.com.gome.logic.model.notice.IssueRevokeMsgModel;
import cn.com.gome.logic.model.notice.QuitGroupMsgModel;

/**
 * 功能性消息
 * CDM:0x020D
 * 为防止与ProtoIM文中的类同名，在后面增加Modle作区分
 */
public class NoticeMsgModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private String msgId; // 消息id
	private int noticeType; // 通知；1:退/踢出群、2:撤销消息通知消息
	private QuitGroupMsgModel quitGroup; // 退/踢出群
	private IssueRevokeMsgModel issueRevoke; // 撤销消息通知消息

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public int getNoticeType() {
		return noticeType;
	}

	public void setNoticeType(int noticeType) {
		this.noticeType = noticeType;
	}

	public QuitGroupMsgModel getQuitGroup() {
		return quitGroup;
	}

	public void setQuitGroup(QuitGroupMsgModel quitGroup) {
		this.quitGroup = quitGroup;
	}

	public IssueRevokeMsgModel getIssueRevoke() {
		return issueRevoke;
	}

	public void setIssueRevoke(IssueRevokeMsgModel issueRevoke) {
		this.issueRevoke = issueRevoke;
	}

}
