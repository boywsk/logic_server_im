package cn.com.gome.logic.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.annotation.HandlerContract;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.model.GroupMsg;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.service.GroupMsgService;

/**
 * 消息广播
 */
@HandlerContract(cmd = Command.CMD_BROADCAST_IM_MSG)
public class BroadcastHandler implements IMsgHandler {
	Logger log = LoggerFactory.getLogger(BroadcastHandler.class);
	
	@Override
	public void process(MQMsg mqMsg) {
		log.info("[broadcast] msg=[{}]", mqMsg);
		String appId = mqMsg.getAppId().trim();
		boolean isPersist = mqMsg.isPersist();
		GroupMsg groupMsg = mqMsg.getGroupMsg();
//		if(Strings.isNullOrEmpty(groupMsg.getGroupId())) {
//			groupMsg.setGroupId("" + groupMsg.getSenderId());
//		}
		GroupMsgService service = new GroupMsgService();
//		groupMsg.setGroupType(Constant.CHAT_TYPE.SYS.value);
		service.broadcastMsg(appId, isPersist, groupMsg);
	}
	
	@Override
	public void process(String clientKey, ProtocolPackage msg) {
		// TODO Auto-generated method stub
		
	}
}
