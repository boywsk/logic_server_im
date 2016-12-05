package cn.com.gome.logic.client;

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
public class TCPProtocolEncoder extends MessageToMessageEncoder<ProtocolPackage> {

	private static final Logger log = LoggerFactory.getLogger(TCPProtocolEncoder.class);

	protected void encode(ChannelHandlerContext ctx, ProtocolPackage msg, List<Object> out) throws Exception {
		short cmd = msg.getCommand();
		long uid = msg.getUid();
		int tranceId = msg.getTraceId();
		long receiveId = msg.getReceiveId();
		log.info("[encode] tranceId=[{}],cmd=[{}],uid=[{}],receiveId=[{}]", tranceId, cmd, uid, receiveId);
		try {
			long packLength = Global.PACK_HEAD_LENGTH + 2;
			if(msg.getProtoBody() != null) {
				packLength = Global.PACK_HEAD_LENGTH + 2 + msg.getProtoBody().length;
			}
			//判断包长是否越界；如果越界丢弃
			if(packLength > Short.MAX_VALUE) {
				log.error("[encode] pack is too larger!!!packLength=[{}]", packLength);
				return;
			}
			log.info("[encode] tranceId=[{}],packLength=[{}]", tranceId, packLength);
			
			ByteBuf buf = Unpooled.buffer((int)packLength);
			buf.writeByte(Global.HEAD_START_TAG);
			byte[] head = msg.getHead();
			buf.writeBytes(head);
			buf.writerIndex(1);
			buf.writeShort((short)packLength);
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
			
//			//appId
//			String appId = msg.getAppId();
//			if(StringUtils.isEmpty(appId)) {
//				ByteBuf appIdBuf = Unpooled.buffer(Global.APPID_LENGTH);
//				appIdBuf.writeBytes(appId.getBytes("UTF-8"));
//				buf.writeBytes(appIdBuf);
//			}
			
			buf.writerIndex(Global.PACK_HEAD_LENGTH + 1);
			if (msg.getProtoBody() != null) {
				buf.writeBytes(msg.getProtoBody());
				log.info("[encode] tranceId=[{}],body size=[{}],uid=[{}],receiveId=[{}]",
						tranceId, msg.getProtoBody().length, uid, receiveId);
			}

			buf.writeByte(Global.HEAD_END_TAG);

			out.add(buf);
			
			
//			ByteBuf buf2 = buf.copy(1, 96);
//			buf2.readerIndex(0);
//			short length2 = buf2.readShort();
//			short cmd2 = buf2.readShort();
//			long uid2 = buf2.readLong();
//			buf2.readerIndex(24);
//			long receiveId2 = buf2.readLong();
//			buf2.readerIndex(44);
//			int traceId2 = buf2.readInt();
//			buf2.readerIndex(49);
//			byte[] appIdBytes = new byte[32];
//			buf2.readBytes(appIdBytes);
//			String appId2 = new String(appIdBytes);
//			log.info("[encode] length2=[{}],appId2=[{}],cmd2=[{}],uid2=[{}],receiveId2=[{}],traceId2=[{}]",
//					length2, appId2, cmd2, uid2, receiveId2, traceId2);
//			log.info("[encode] head byte content=[{}]", Arrays.toString(buf2.array()));
//			buf2 = null;
			
//			buf.readerIndex(1);
//			short length = buf.readShort();

			log.info("[encode] tranceId=[{}],cmd=[{}],uid=[{}],head size=[{}]",
					tranceId, cmd, uid, head.length);
		} catch (Exception e) {
			log.error("[encoder] cause is:", e);
		}
	}
}