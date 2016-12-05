package cn.com.gome.logic.test.tcp.client;

import cn.com.gome.logic.global.Global;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
public class ProtoHead {
	
	public static ByteBuf head(short length, short cmd, long uid, String appId) {
		ByteBuf head = Unpooled.buffer(Global.PACK_HEAD_LENGTH);
		try {
			head.writeShort(length + Global.PACK_HEAD_LENGTH);
			head.writeShort(cmd);
			head.writeLong(uid);
			head.writeByte((byte)10);
			head.writeByte((byte)1);
			head.writeByte((byte)0);
			head.writeByte((byte)0);
			head.writeLong(System.currentTimeMillis());
			head.writeLong(0L);
			head.writeLong(0L);
			head.writeInt(0);
			ByteBuf appIdBytes = Unpooled.buffer(32);
			appIdBytes.writeBytes(appId.getBytes("UTF-8"));
			head.writeBytes(appIdBytes);
			head.writeBytes(new byte[20]);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return head;
	}
}
