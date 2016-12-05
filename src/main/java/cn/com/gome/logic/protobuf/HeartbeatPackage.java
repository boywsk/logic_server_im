package cn.com.gome.logic.protobuf;

import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.utils.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 心跳包
 */
public class HeartbeatPackage {

	private final static short CMD_HEARTBEAT = 0x0001;
	private final static byte CLIENT_ID = 0;
	private final static byte VERSION = 1;
	private final static byte CLIENT_TYPE = 0;
	private final static byte RESULT = 0;

	public static ProtocolPackage encoderHeartbeatPackage() {
		ProtocolPackage msg = new ProtocolPackage();

		byte[] body = encoderHeartbeat();
		byte[] head = encoderHeader(body, CMD_HEARTBEAT);
		msg.setHead(head);
		msg.setClientId(CLIENT_ID);
		msg.setCommand(CMD_HEARTBEAT);
		msg.setiVersion(VERSION);
		msg.setProtoBody(encoderHeartbeat());
		msg.setTraceId(StringUtils.getRanNumber());
		msg.setResult(RESULT);
		msg.setUid(0L);
		return msg;
	}

	private static byte[] encoderHeader(byte[] data, short cmd) {
		short headLen = Global.PACK_HEAD_LENGTH;
		ByteBuf buf = Unpooled.buffer(headLen);
		buf.writeShort(headLen + data.length);
		buf.writeShort(cmd);
		buf.writeLong(0L);
		buf.writeByte(CLIENT_ID);
		buf.writeByte(VERSION);
		buf.writeByte(CLIENT_TYPE);
		buf.writeByte(RESULT);
		buf.writeLong(System.currentTimeMillis());
		buf.writeLong(0L);
		buf.writeLong(0L);
		buf.writeInt(0);
		buf.writeByte(0);
		buf.writeBytes(new byte[32]);
		buf.writeBytes(new byte[15]);
		return buf.array();
	}

	private static byte[] encoderHeartbeat() {
		ProtoIM.Heartbeat.Builder builder = ProtoIM.Heartbeat.newBuilder();
		builder.setUid(0L);
		return builder.build().toByteArray();
	}
}
