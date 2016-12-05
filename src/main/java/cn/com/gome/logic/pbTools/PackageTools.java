package cn.com.gome.logic.pbTools;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.utils.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 聊天报文工具类
 */
public class PackageTools {
	static Logger log = LoggerFactory.getLogger(PackageTools.class);
	
	/**
	 * 生成协议头
	 * @param appId
	 * @param uid
	 * @param cmd
	 * @return
	 */
	public static byte[] generateHead(String appId, long uid, short cmd) {
		ByteBuf head = Unpooled.buffer(Global.PACK_HEAD_LENGTH);
		head.writeShort(0);//encoder计算长度
		head.writeShort(cmd);
		head.writeLong(uid);
		head.writeByte(0);
		head.writeByte(0);
		head.writeByte(0);
		head.writeByte(0);
		head.writeLong(System.currentTimeMillis());
		head.writeLong(0);
		head.writeLong(0);
		head.writeInt(0);
		head.writeInt(StringUtils.getRanNumber());//traceId
		head.writeByte(0);//ack
		ByteBuf appIdBuf = Unpooled.buffer(Global.APPID_LENGTH);
		try {
			appIdBuf.writeBytes(appId.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			log.error("[generatePack]:", e);
		}
		head.writeBytes(appIdBuf);
		
		return head.array();
	}
}
