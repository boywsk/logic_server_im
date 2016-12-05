package cn.com.gome.logic.test.tcp.client;

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
		log.info("cmd is {},uid is {}", msg.getCommand(), msg.getUid());
		try {
			ByteBuf buf = Unpooled.buffer();
			buf.writeByte(Global.HEAD_START_TAG);
			byte[] head = msg.getHead();
			buf.writeBytes(head);
			if (msg.getProtoBody() != null) {
				buf.writerIndex(1);
				buf.writeShort(Global.PACK_HEAD_LENGTH + 2 + msg.getProtoBody().length); // 98 +
			} else {
				buf.writerIndex(1);
				buf.writeShort(Global.PACK_HEAD_LENGTH + 2); // 98
			}
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
			
			String appId = msg.getAppId();
			if(appId != null) {
				buf.writerIndex(45);
				buf.writeBytes(appId.getBytes("UTF-8"));
			}

			buf.writerIndex(Global.PACK_HEAD_LENGTH + 1);
			if (msg.getProtoBody() != null) {
				buf.writeBytes(msg.getProtoBody());
			}

			buf.writeByte(Global.HEAD_END_TAG);

			out.add(buf);

			log.info("cmd = [{}],uid = [{}],head size = [{}],body size = [{}]", msg.getCommand(), msg.getUid(),
					head.length, msg.getProtoBody().length);
		} catch (Exception e) {
			log.error("[encoder] error ,cause is {}", e);
		}
		log.info("end...,cmd = [{}],uid = [{}]", msg.getCommand(), msg.getUid());
	}
}