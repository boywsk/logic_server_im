package cn.com.gome.logic.test.tcp.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.cache.ServerCache;
import cn.com.gome.logic.protobuf.HeartbeatPackage;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.utils.IPUtils;

/**
 * client消息处理器
 */
public class TcpClientHandler extends ChannelHandlerAdapter {
	private static final Logger log = LoggerFactory.getLogger(TcpClientHandler.class);

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.info("channel id = [{}]", ctx.channel().id());
		try {
			InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
			String clientIp = address.getAddress().getHostAddress();
			int port = address.getPort();
			log.info("ip=[{}],port=[{}]", clientIp, port);
			String clientKey = IPUtils.ipToLong(clientIp) + ":" + port;
			ServerCache.removeRespChannel(clientKey);
			ctx.close();
		} catch (Exception e) {
			log.error("cause is:", e);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.info("channel id = [{}]", ctx.channel().id());
		try {
			InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
			String clientIp = address.getAddress().getHostAddress();
			int port = address.getPort();
			String clientKey = IPUtils.ipToLong(clientIp) + ":" + port;
			ServerCache.getChannel(clientKey).close();
			ServerCache.removeRespChannel(clientKey);
		} catch (Exception e) {
			log.error("cause is:",  e);
		} finally {
			ctx.close();
		}
		log.info("[exceptionCaught]end...,channel id = [{}]", ctx.channel().id());
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		log.info("start...,channel id = [{}],channel status = [{}]", ctx.channel().id(),
				ctx.channel().isActive());
		if (evt instanceof IdleStateEvent) {
			log.info("evt = [{}]", evt.getClass().getName());
			IdleStateEvent e = (IdleStateEvent) evt;
			log.info("evt status = [{}]", e.state().toString());
			if (e.state() == IdleState.WRITER_IDLE) {
				ProtocolPackage pack = HeartbeatPackage.encoderHeartbeatPackage();
				ctx.channel().writeAndFlush(pack).addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture f) throws Exception {
						// TODO Auto-generated method stub
						if (f.isSuccess()) {
							log.info("send heartbeat to gateway success");
						} else {
							log.info("send heartbeat to gateway failed");
						}
					}
				});

			}
		}
	}
}
