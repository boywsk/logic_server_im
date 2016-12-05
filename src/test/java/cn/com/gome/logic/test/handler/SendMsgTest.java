package cn.com.gome.logic.test.handler;

import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Constant.CHAT_TYPE;
import cn.com.gome.logic.global.Constant.MESSAGE_TYPE;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.test.tcp.client.Client;
import cn.com.gome.logic.test.tcp.client.ProtoHead;
import cn.com.gome.logic.utils.StringUtils;
import cn.com.gome.logic.protobuf.ProtoIM.ImMsg;

public class SendMsgTest {

	public static void main(String[] args) {
		ImMsg.Builder pbMsgBuilder = ImMsg.newBuilder();
		pbMsgBuilder.setMsgId(StringUtils.getUuid());
		pbMsgBuilder.setMsgType(MESSAGE_TYPE.TEXT.value);
		pbMsgBuilder.setMsgBody("hello");
		pbMsgBuilder.setSenderId(10000L);
		pbMsgBuilder.setSenderName("lxm");
		//pbMsgBuilder.setSenderAvatar("avatar");
		pbMsgBuilder.setGroupId("2ad4c601de8e4c32b45b668d7d0b6ad0");
		pbMsgBuilder.setGroupType(CHAT_TYPE.GROUP.value);
		pbMsgBuilder.setGroupName("群组111");

		ImMsg pbMsg = pbMsgBuilder.build();
		short length = (short)pbMsg.getSerializedSize();
		ProtocolPackage pack = new ProtocolPackage();
		pack.setStartTag((byte)2);
		pack.setAppId("TEST_APP_ID");
		pack.setReceiveId(2000L);
		short cmd = Command.CMD_SUBMIT_INIT_SEQ;
		pack.setHead((ProtoHead.head(length, cmd, 1000L, "TEST_APP_ID")).array());
		byte[] protoBuf = pbMsg.toByteArray();
		pack.setProtoBody(protoBuf);
		pack.setEndTag((byte)3);
		
		new Client().sendMsg(pack);
		while(true) {
			
		}
	}
}
