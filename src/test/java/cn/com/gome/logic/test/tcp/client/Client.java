package cn.com.gome.logic.test.tcp.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class Client {
	static Logger log = LoggerFactory.getLogger(Client.class);
	private static Channel channel = null;
	
	static {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.option(ChannelOption.SO_RCVBUF, 1024000)
					.option(ChannelOption.SO_SNDBUF, 1024000)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline pipeline = ch.pipeline();
							pipeline.addLast(new TCPProtocolDecoder());// 解码器
							pipeline.addLast(new TCPProtocolEncoder());// 编码器
							pipeline.addLast("idleStateHandler", new IdleStateHandler(0, Global.HEARTBEAT_TIME - 5, 0));// 定时器,秒
							pipeline.addLast(new TcpClientHandler());
						}
					});
			ChannelFuture f = b.connect("10.125.3.11", 8000).sync();//10.69.16.56  10.125.3.11
			if (f.isSuccess()) {
				channel = f.channel();
			} else {
				log.error("connect:", f.cause());
			}
		} catch (Exception e) {
			log.error("connect:", e);
		}
	}
	
	public void sendMsg(ProtocolPackage pack) {
//		EventLoopGroup group = new NioEventLoopGroup();
//		try {
//			Bootstrap b = new Bootstrap();
//			b.group(group).channel(NioSocketChannel.class)
//					.option(ChannelOption.TCP_NODELAY, true)
//					.option(ChannelOption.SO_KEEPALIVE, true)
//					.option(ChannelOption.SO_RCVBUF, 1024000)
//					.option(ChannelOption.SO_SNDBUF, 1024000)
//					.handler(new ChannelInitializer<SocketChannel>() {
//						@Override
//						protected void initChannel(SocketChannel ch) throws Exception {
//							ChannelPipeline pipeline = ch.pipeline();
//							pipeline.addLast(new ClientProtocolDecoder());// 解码器
//							pipeline.addLast(new ClientProtocolEncoder());// 编码器
////							pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 10, 0));//定时器,秒
//							pipeline.addLast(new ClientHandler());
//						}
//					});
//			ChannelFuture f = b.connect("127.0.0.1", 8000).sync();
//			if (f.isSuccess()) {
//				Channel channel = f.channel();
//				channel.writeAndFlush(pack);
//				while(true) {
//					
//				}
//			} else {
//				log.error("connect:", f.cause());
//			}
//		} catch (Exception e) {
//			log.error("connect:", e);
//		}
		try {
			channel.writeAndFlush(pack);
		} catch (Exception e) {
			log.error("sendMsg:", e);
		}
	}
}
