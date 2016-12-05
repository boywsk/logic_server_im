package cn.com.gome.logic.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.handler.IMsgHandler;
import cn.com.gome.logic.cache.ServerCache;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.utils.IPUtils;
import cn.com.gome.logic.utils.TcpDataSendUtils;

/**
 * 服务器处理器
 */
public class ServerHandler extends ChannelHandlerAdapter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		log.info("[channelRead] channel id=[{}]", ctx.channel().id());
		Channel channel = ctx.channel();
		InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
		String clientIp = address.getHostString();
		int port = address.getPort();
		String clientKey = IPUtils.ipToLong(clientIp) + ":" + port;
		ProtocolPackage message = (ProtocolPackage) msg;
		short cmd = message.getCommand();
		log.info("[channelRead] cmd=[{}],remote ip=[{}],clientKey=[{}]", cmd, clientIp, clientKey);
		IMsgHandler handler = ServerCache.getBaseHandler(cmd);
		if (null != handler) {
			handler.process(clientKey, message);
		} else {
			log.error("[channelRead] handler is null,so drop this message");
			message.setResult((byte) 250);
			message.setProtoBody(null);
			// 返回错误信息报
			TcpDataSendUtils.sendTcpData(clientKey, message);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.info("[channelActive] channel id=[{}]", ctx.channel().id());
		Channel channel = ctx.channel();
		InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
		String clientIp = address.getHostString();
		int port = address.getPort();
		String clientKey = IPUtils.ipToLong(clientIp) + ":" + port;
		ServerCache.setChannel(clientKey, channel);
		log.info("[channelActive] channel id=[{}],remote ip=[{}],port=[{}]", ctx.channel().id(), clientIp, port);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.info("[channelInactive] channel id=[{}]", ctx.channel().id());
		InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
		String clientIp = address.getHostString();
		int port = address.getPort();
		String clientKey = IPUtils.ipToLong(clientIp) + ":" + port;
		ServerCache.removeChannel(clientKey);
		ctx.channel().close();
		ctx.close();
		ctx = null;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.info("[exceptionCaught]:", cause);
		try {
			// 发送监控数据
			// DataSendManager.sendAttrData(3);
			Channel channel = ctx.channel();
			InetSocketAddress address = (InetSocketAddress)channel.remoteAddress();
			String clientIp = address.getHostString();
			int port = address.getPort();
			String clientKey = IPUtils.ipToLong(clientIp) + ":" + port;
			
			if(channel != null) {
				if(channel.isOpen() && channel.isActive()) {
					log.info("[exceptionCaught] channel is ok!!!");
				} else {
					ServerCache.removeChannel(clientKey);
					channel.close();
					ctx.close();
					channel = null;
					ctx = null;
				}
			}
			log.info("[exceptionCaught] channel id=[{}],clientIp=[{}],clientKey=[{}]", ctx.channel().id(), clientIp,
					clientKey);
		} catch (Exception e) {
			log.error("[exceptionCaught] error is:", e);
		}
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		log.info("[userEventTriggered] channel id=[{}]", ctx.channel().id());
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.READER_IDLE) {
				Channel channel = ctx.channel();
				InetSocketAddress address = (InetSocketAddress)channel.remoteAddress();
				String clientIp = address.getHostString();
				int port = address.getPort();
				log.info("[userEventTriggered] remote ip=[{}],port=[{}]", clientIp, port);
				String clientKey = IPUtils.ipToLong(clientIp) + ":" + port;
				log.info("[userEventTriggered] 心跳超时......");
				ServerCache.removeChannel(clientKey);
				if (channel != null) {
					channel.close();
					channel = null;
				}
				ctx.close();
				ctx = null;
			} 
		}
	}
}
