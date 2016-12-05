package cn.com.gome.logic.client;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.cache.ServerCache;
import cn.com.gome.logic.protobuf.HeartbeatPackage;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.utils.IPUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * tcp client Connection消息处理器
 */
public class ConnectionHandler extends ChannelHandlerAdapter {
	private static final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.info("[channelInactive] channel id=[{}]", ctx.channel().id());
		try {
			Channel channel = ctx.channel();
			InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
			String clientIp = address.getAddress().getHostAddress();
			int port = address.getPort();
			InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
			int localPort = localAddress.getPort();
			log.info("[channelInactive] clientIp=[{}],port=[{}],localPort=[{}]", clientIp, port, localPort);
			String clientKey = IPUtils.ipToLong(clientIp) + ":" + port;
			channel.close();
			Connection connection = ServerCache.getConnection(clientKey, channel.id());
			connection.close();
			ServerCache.removeConnection(clientKey, channel.id());
			channel = null;
			ctx.close();
			ctx = null;
		} catch (Exception e) {
			log.error("[channelInactive] cause is:", e);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.info("[exceptionCaught] channel id=[{}]", ctx.channel().id());
		log.error("[exceptionCaught]", cause);
		try {
			Channel channel = ctx.channel();
			InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
			String clientIp = address.getAddress().getHostAddress();
			int port = address.getPort();
			String clientKey = IPUtils.ipToLong(clientIp) + ":" + port;
			if (channel != null) {
				if (channel.isWritable() && channel.isActive() && channel.isOpen()) {
					log.info("[exceptionCaught] channel is ok!!!");
				} else {
					channel.close();
					ctx.close();
					Connection connection = ServerCache.getConnection(clientKey, channel.id());
					connection.close();
					ServerCache.removeConnection(clientKey, channel.id());
					channel = null;
					ctx = null;
				}
			}
		} catch (Exception e) {
			log.error("[exceptionCaught] cause is:", e);
		}
	}

	@Override
	public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {
		log.info("[userEventTriggered] channel id=[{}],channel status=[{}]", ctx.channel().id(),
				ctx.channel().isActive());
		InetSocketAddress localAddress = (InetSocketAddress)ctx.channel().localAddress();
		String localIp = localAddress.getHostString();
		int localPort = localAddress.getPort();
		log.info("[userEventTriggered] localIp=[{}],localPort=[{}]", localIp, localPort);
		if (evt instanceof IdleStateEvent) {
			log.info("[userEventTriggered] evt=[{}]", evt.getClass().getName());
			IdleStateEvent e = (IdleStateEvent) evt;
			log.info("[userEventTriggered] evt status=[{}]", e.state().toString());
			if (e.state() == IdleState.ALL_IDLE) {
				ProtocolPackage pack = HeartbeatPackage.encoderHeartbeatPackage();
				int tranceId = pack.getTraceId();
				ctx.channel().writeAndFlush(pack).addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture f) {
						if (f.isSuccess()) {
							log.info("[userEventTriggered] send heartbeat to gateway success,tranceId=[{}]", tranceId);
						} else {
							Channel channel = ctx.channel();
							log.info("[userEventTriggered] send heartbeat to gateway failed,tranceId=[{}]", tranceId);
							InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
							String clientIp = address.getAddress().getHostAddress();
							int port = address.getPort();
							log.info("[userEventTriggered] tranceId=[{}],ip=[{}],port=[{}]", tranceId, clientIp, port);
							String clientKey = IPUtils.ipToLong(clientIp) + ":" + port;
							if (channel != null) {
								channel.close();
								Connection connection = ServerCache.getConnection(clientKey, channel.id());
								connection.close();
								ServerCache.removeConnection(clientKey, channel.id());
							}
							ctx.close();
							channel = null;
						}
					}
				});
			}
		}
	}
}
