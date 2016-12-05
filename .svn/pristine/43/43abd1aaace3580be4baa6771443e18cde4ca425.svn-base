package cn.com.gome.logic.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.protobuf.ProtocolPackage;

/**
 * 组包
 */
public class LogicProtocolEncoder extends MessageToMessageEncoder<ProtocolPackage> {
	private static final Logger log = LoggerFactory.getLogger(LogicProtocolEncoder.class);

	protected void encode(ChannelHandlerContext ctx, ProtocolPackage msg, List<Object> out) throws Exception {
		int tranceId = msg.getTraceId();
		log.info("[encode] tranceId=[{}],cmd=[{}],uid=[{}]", tranceId, msg.getCommand(), msg.getUid());
		long packLength = Global.PACK_HEAD_LENGTH + 2;
		if(msg.getProtoBody() != null) {
			packLength = Global.PACK_HEAD_LENGTH + 2 + msg.getProtoBody().length;
		}
		//判断包长是否越界；如果越界丢弃
		if(packLength > Short.MAX_VALUE) {
			log.error("[encode] pack is too larger!!!packLength=[{}]", packLength);
			return;
		}
		
		ByteBuf buf = Unpooled.buffer((int)packLength);
		buf.writeByte(Global.HEAD_START_TAG);
		byte[] head = msg.getHead();
		buf.writeBytes(head);
		log.info("[encode] cmd=[{}],uid=[{}],head size=[{}]", msg.getCommand(), msg.getUid(), head.length);
//		if (msg.getProtoBody() != null) {
//			buf.writerIndex(1);
//			short packLength = (short)(Global.PACK_HEAD_LENGTH + 2 + msg.getProtoBody().length);
//			log.info("[encode] tranceId=[{}],cmd=[{}],uid=[{}],packLength=[{}]", tranceId, msg.getCommand(), msg.getUid(), packLength);
//			buf.writeShort(packLength); // 98 +
//		} else {
//			buf.writerIndex(1);
//			buf.writeShort(Global.PACK_HEAD_LENGTH + 2); // 98
//		}
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

		out.add(buf);
	}
}