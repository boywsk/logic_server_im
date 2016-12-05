package cn.com.gome.logic.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.protobuf.ProtocolPackage;

/**
 * 编码器
 */
public class LogicProtocolDecoder extends ByteToMessageDecoder {
	private static final Logger log = LoggerFactory.getLogger(LogicProtocolDecoder.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
		try {
			ProtocolPackage message = new ProtocolPackage();
			buf.markReaderIndex();

			// 判断包头长度
			if (buf.readableBytes() < (Global.PACK_HEAD_LENGTH + 1)) {// 不够包头长度97
				buf.resetReaderIndex();
				log.info("[decode] package len not enough");
				return;
			}

			byte startTag = buf.readByte();
			if (startTag != Global.HEAD_START_TAG) {
				log.error("[decode] msg is illegal");
				ctx.close();
				return;
			}

			ByteBuf head = buf.readBytes(Global.PACK_HEAD_LENGTH);
			short totalLength = head.readShort();
			log.info("[decode] data total length=[{}]", totalLength);
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
			String appId = new String(head.readBytes(Global.APPID_LENGTH).array(), "UTF-8");

			// 计算有效数据长度
			int leftLength = totalLength - Global.PACK_HEAD_LENGTH - 1;
			if (buf.readableBytes() < leftLength) {// 长度不够
				buf.resetReaderIndex();
				log.info("[decode] body len not enough");
				return;
			}

			int bodyLength = leftLength - 1;
			ByteBuf protoBody = buf.readBytes(bodyLength);
			log.info("[decode] cmd=[{}],[decode] head size=[{}],body size=[{}]", cmd, head.array().length, protoBody.array().length);
			byte endTag = buf.readByte();

			// 封装协议对象
			message.setHead(head.array());
			message.setStartTag(startTag);
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
			message.setTraceId(traceId);
			message.setAck(ack);
			message.setAppId(appId.trim());
			message.setProtoBody(protoBody.array());
			message.setEndTag(endTag);

			out.add(message);
		} catch (Exception e) {
			log.error("[decode] LogicProtocolDecoder:", e);
		}
	}
}
