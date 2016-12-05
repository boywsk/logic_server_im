package cn.com.gome.logic.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import cn.com.gome.logic.annotation.HandlerContract;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.service.NoticeMsgService;
import cn.com.gome.logic.utils.StringUtils;

/**
 * 通知消息
 */
@HandlerContract(cmd = Command.CMD_NOTICE_MSG)
public class NoticeMsgHandler implements IMsgHandler {
	public static final Logger log = LoggerFactory.getLogger(NoticeMsgHandler.class);

	@Override
	public void process(MQMsg msg) {
		int traceId = StringUtils.getRanNumber();
		if(!Strings.isNullOrEmpty(msg.getAppId())){
			log.info("[NoticeMsgHandler] Start handler. traceId=[{}]",traceId);
			NoticeMsgService noticeMsgService = new NoticeMsgService();
			noticeMsgService.dispatchNoticeMsg(msg, traceId);
		}else{
			log.info("[NoticeMsgHandler] Start handler error, GroupMsg is NULL! traceId=[{}]",traceId);
		}
	}

	@Override
	public void process(String clientKey, ProtocolPackage msg) {
		// TODO Auto-generated method stub

	}
}
