package cn.com.gome.logic.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * ProtocolPackage转换工具类
 */
public class ProtocolPackageUtils {
	static Logger log = LoggerFactory.getLogger(ProtocolPackageUtils.class);
	
	/**
	 * 字符数组转ProtocolPackage
	 * @param data
	 * @return
	 */
	public static ProtocolPackage byteArray2Package(byte[] data) {
		ProtocolPackage message = new ProtocolPackage();
		//ByteBuf buf = Unpooled.buffer(data.length);
		ByteBuf buf = Unpooled.copiedBuffer(data);
		byte startTag = buf.readByte();
		if (startTag != Global.HEAD_START_TAG) {
			return null;
		}
		ByteBuf head = buf.readBytes(Global.PACK_HEAD_LENGTH);
		/*ByteBuf head = buf.readBytes(Global.PACK_HEAD_LENGTH);
		byte startTag = head.readByte();
		if (startTag != Global.HEAD_START_TAG) {
			return null;
		}*/
		short totalLength = head.readShort();
		short cmd = head.readShort();
		long uid = head.readLong();
		byte clientId = head.readByte();
		byte iVersion = head.readByte();
		byte clientType = head.readByte();
		byte result = head.readByte();
		long sTime = head.readLong();
		long receverId = head.readLong();
		long rspIP = head.readLong();
		int rspPort = head.readInt();
		int traceId = head.readInt();	
		byte ack = head.readByte();
		String appId = new String(head.readBytes(Global.APPID_LENGTH).array());
		int bodyLength = totalLength - Global.PACK_HEAD_LENGTH - 2;
		ByteBuf protoBody = buf.readBytes(bodyLength);
		byte endTag = buf.readByte();
		// 封装协议对象
		message.setStartTag(startTag);
		message.setHead(head.array());
		message.setLength(totalLength);
		message.setCommand(cmd);
		message.setUid(uid);
		message.setClientId(clientId);
		message.setiVersion(iVersion);
		message.setClientType(clientType);
		message.setResult(result);
		message.setStime(sTime);
		message.setReceiveId(receverId);
		message.setRspIP(rspIP);
		message.setRspPort(rspPort);
		message.setAppId(appId);
		message.setTraceId(traceId);
		message.setAck(ack);
		message.setProtoBody(protoBody.array());
		message.setEndTag(endTag);
		return message;
	}
	
	/**
	 * ProtocolPackage转换字节数组
	 * @param msg
	 * @return
	 */
	public static byte[] package2Byte(ProtocolPackage msg) {
//		byte[] body = msg.getProtoBody();
//		ByteBuf buf = Unpooled.buffer();
//		buf.writeByte(msg.getStartTag());
//		buf.writeBytes(msg.getHead());
//		buf.writeByte(msg.getEndTag());
//		if (body != null) {
//			buf.writerIndex(1);
//			buf.writeShort(Global.PACK_HEAD_LENGTH + 2 + msg.getProtoBody().length); // 98 +
//		} else {
//			buf.writerIndex(1);
//			buf.writeShort(Global.PACK_HEAD_LENGTH + 2); // 98
//		}
//		buf.writerIndex(25);
//		buf.writeLong(msg.getReceiveId());
//		buf.writerIndex(Global.PACK_HEAD_LENGTH + 1);
//		if (body != null) {
//			buf.writeBytes(body);
//			log.info("[package2Byte] body size=[{}]", msg.getProtoBody().length);
//		}
//		buf.writeByte(Global.HEAD_END_TAG);
		
		long packLength = Global.PACK_HEAD_LENGTH + 2;
		if(msg.getProtoBody() != null) {
			packLength = Global.PACK_HEAD_LENGTH + 2 + msg.getProtoBody().length;
		}
		//判断包长是否越界；如果越界丢弃
		if(packLength > Short.MAX_VALUE) {
			log.error("[package2Byte] pack is too larger!!!packLength=[{}]", packLength);
			return null;
		}
		ByteBuf buf = Unpooled.buffer((int)packLength);
		buf.writeByte(Global.HEAD_START_TAG);
		byte[] head = msg.getHead();
		buf.writeBytes(head);
		log.info("[package2Byte] cmd=[{}],uid=[{}],head size=[{}]", msg.getCommand(), msg.getUid(), head.length);
		buf.writerIndex(1);
		buf.writeShort((short)packLength);
		short cmd = msg.getCommand();
		if (cmd > 0) {
			buf.writerIndex(3);
			buf.writeShort(cmd);
		}
		byte clientId = msg.getClientId();
		if (clientId > 0) {
			buf.writerIndex(13);
			buf.writeByte(clientId);
		}
		byte result = msg.getResult();
		buf.writerIndex(16);
		buf.writeByte(result);
		long receverId = msg.getReceiveId();
		buf.writerIndex(25);
		buf.writeLong(receverId);
		//rsp ip
		buf.writeLong(0);
		//rsp port
		buf.writeInt(0);
		//traceId
		int traceId = msg.getTraceId();
		if (traceId > 0) {
			buf.writerIndex(45);
			buf.writeInt(traceId);
		}
		//ack
		byte ack = msg.getAck();
		if(ack > 0) {
			buf.writerIndex(49);
			buf.writeByte(ack);
		}
		buf.writerIndex(Global.PACK_HEAD_LENGTH + 1);
		if (msg.getProtoBody() != null) {
			buf.writeBytes(msg.getProtoBody());
		}
		buf.writeByte(Global.HEAD_END_TAG);
		return buf.array();
	}
}
