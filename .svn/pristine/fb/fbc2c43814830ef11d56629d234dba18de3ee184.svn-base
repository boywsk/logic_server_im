package cn.com.gome.logic.client;

import io.netty.channel.Channel;
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
		log.info("[channelInactive] channel id=[{}]", ctx.channel().id());
		try {
			InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
			String clientIp = address.getAddress().getHostAddress();
			int port = address.getPort();
			String clientKey = IPUtils.ipToLong(clientIp) + ":" + port;
			log.info("[channelInactive] channel clientKey=[{}]", clientKey);
			Channel channel = ServerCache.getRespChannel(clientKey);
			if(channel != null) {
				ServerCache.removeRespChannel(clientKey);
				channel.close();
				channel= null;
			} else {
				log.info("[channelInactive] channel is null!!!");
			}
		} catch (Exception e) {
			log.error("[channelInactive] cause is:", e);
		} finally {
			ctx.close();
			ctx = null;
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
			log.info("[exceptionCaught] channel clientKey=[{}]", clientKey);
			if(channel != null) {
				if(channel.isWritable() && channel.isActive() && channel.isOpen()) {
					log.info("[exceptionCaught] channel is ok!!!");
				} else {
					ServerCache.removeRespChannel(clientKey);
					channel.close();
					ctx.close();
					channel= null;
					ctx = null;
				}
			}
		} catch (Exception e) {
			log.error("[exceptionCaught] cause is:",  e);
		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		log.info("[userEventTriggered] channel id=[{}],channel status=[{}]", ctx.channel().id(),
				ctx.channel().isActive());
		if (evt instanceof IdleStateEvent) {
			log.info("[userEventTriggered] evt=[{}]", evt.getClass().getName());
			IdleStateEvent e = (IdleStateEvent) evt;
			log.info("[userEventTriggered] evt status=[{}]", e.state().toString());
			InetSocketAddress localAddress = (InetSocketAddress)ctx.channel().localAddress();
			String localIp = localAddress.getHostString();
			int localPort = localAddress.getPort();
			log.info("[userEventTriggered] localIp=[{}],localPort=[{}]", localIp, localPort);
			if (e.state() == IdleState.ALL_IDLE) {
				ProtocolPackage pack = HeartbeatPackage.encoderHeartbeatPackage();
				ctx.channel().writeAndFlush(pack).addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture f) {
						if (f.isSuccess()) {
							log.info("[userEventTriggered] send heartbeat to gateway success,"
									+ "channel id=[{}]", ctx.channel().id());
						} else {
							log.info("[userEventTriggered] send heartbeat to gateway failed,channel id=[{}]", ctx.channel().id());
							Channel channel = ctx.channel();
							InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
							String clientIp = address.getAddress().getHostAddress();
							int port = address.getPort();
							log.info("[userEventTriggered] ip=[{}],port=[{}]", clientIp, port);
							String clientKey = IPUtils.ipToLong(clientIp) + ":" + port;
							ServerCache.removeRespChannel(clientKey);
							if(channel != null) {
								channel.close();
								channel= null;
							}
						}
					}
				});
			}
		}
	}
}
