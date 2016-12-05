package cn.com.gome.logic.pbTools;

import java.util.List;

import org.slf4j.Logger;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import cn.com.gome.logic.model.notice.IssueRevokeMsgModel;
import cn.com.gome.logic.model.notice.QuitGroupMsgModel;
import cn.com.gome.logic.model.notice.SaveNoticeMsg;
import cn.com.gome.logic.protobuf.ProtoIM.IssueRevokeMsg;
import cn.com.gome.logic.protobuf.ProtoIM.NoticeMsg;
import cn.com.gome.logic.protobuf.ProtoIM.QuitGroupMsg;

public class NoticeMsgTools {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(NoticeMsgTools.class);
	
	/**
	 * NoticeMsg转pb
	 * @param model
	 * @return
	 */
	public static NoticeMsg notice2PbMsg (SaveNoticeMsg model, int traceId) {
		NoticeMsg.Builder msgBuilder = NoticeMsg.newBuilder();
		int noticeType = model.getNoticeType();
		String json = model.getNoticeMsgJson();
		msgBuilder.setNoticeType(noticeType);
		msgBuilder.setMsgId(model.getMsgId());
		switch(noticeType){
		case 1:		//1:退/踢出群
			QuitGroupMsgModel quitGroupMsgModel = JSON.parseObject(json, QuitGroupMsgModel.class);
			QuitGroupMsg quitGroupMsg = quitGroup2PbMsg(quitGroupMsgModel);
			msgBuilder.setQuitGroup(quitGroupMsg);
			break;
		case 2:		//2:撤销消息通知消息
			IssueRevokeMsgModel issueRevokeMsgModel = JSON.parseObject(json, IssueRevokeMsgModel.class);
			IssueRevokeMsg issueRevokeMsg = issueRevoke2PbMsg(issueRevokeMsgModel);
			msgBuilder.setIssueRevoke(issueRevokeMsg);
			break;
		default :
			log.error("[notice2PbMsg] traceId=[{}] Not find noticeType!!!",traceId);
		}
		return msgBuilder.build();
	}
	
	/**
	 * 退/踢出群转pb
	 * @param model
	 * @return
	 */
	public static QuitGroupMsg quitGroup2PbMsg(QuitGroupMsgModel model) {
		QuitGroupMsg.Builder msgBuilder = QuitGroupMsg.newBuilder();
		msgBuilder.setQuitType(model.getQuitType());
		msgBuilder.setFromUid(model.getFromUid());
		String fromName = model.getFromName();
		if(!Strings.isNullOrEmpty(fromName)) {
			msgBuilder.setFromName(fromName);
		}
		List<Long> uids = model.getKickedUids();
		if(uids != null) {
			msgBuilder.addAllKickedUids(uids);
		}
		List<String> names = model.getKickedNames();
		if(names != null) {
			msgBuilder.addAllKickedNames(names);
		}
		msgBuilder.setGroupId(model.getGroupId());
		String content = model.getContent();
		if(!Strings.isNullOrEmpty(content)) {
			msgBuilder.setContent(content);
		}
		msgBuilder.setOptTime(model.getOptTime());
		String extra = model.getExtra();
		if(!Strings.isNullOrEmpty(extra)) {
			msgBuilder.setExtra(extra);
		}
		return msgBuilder.build();
	}
	
	/**
	 * 撤消消息转pb
	 * @param model
	 * @return
	 */
	public static IssueRevokeMsg issueRevoke2PbMsg(IssueRevokeMsgModel model) {
		IssueRevokeMsg.Builder msgBuilder = IssueRevokeMsg.newBuilder();
		long uid = model.getUid();
		if(uid > 0){
			msgBuilder.setUid(uid);
		}
		String groupId = model.getGroupId();
		if(!Strings.isNullOrEmpty(groupId)){
			msgBuilder.setGroupId(groupId);
		}
		String msgId = model.getMsgId();
		if(!Strings.isNullOrEmpty(msgId)) {
			msgBuilder.setMsgId(msgId);
		}
		msgBuilder.setOptTime(model.getOptTime());
		String extra = model.getExtra();
		if(!Strings.isNullOrEmpty(extra)) {
			msgBuilder.setExtra(extra);
		}
		return msgBuilder.build();
	}
}
