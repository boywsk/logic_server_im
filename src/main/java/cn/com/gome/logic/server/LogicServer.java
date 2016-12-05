package cn.com.gome.logic.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.global.Global;

/**
 * 逻辑服务
 */
public class LogicServer {
	
	private final Logger log = LoggerFactory.getLogger(LogicServer.class);
	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;

	public LogicServer(int workerNum) {
		bossGroup = new NioEventLoopGroup();
//		workerGroup = new NioEventLoopGroup(workerNum);
		workerGroup = new NioEventLoopGroup();
	}

	public void startServer(String host, int port) {
		try {
			log.info("[startServer] host=[{}],listener port=[{}], heartbeat=[{}]S", host, port, Global.HEARTBEAT_TIME);
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup);

			bootstrap.channel(NioServerSocketChannel.class);
			bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					pipeline.addLast("msgDecoder", new LogicProtocolDecoder());// 解码器
					pipeline.addLast("msgEncoder", new LogicProtocolEncoder());// 编码器
					pipeline.addLast("idleStateHandler", new IdleStateHandler(Global.HEARTBEAT_TIME, 0, 0));// 定时器,秒
					pipeline.addLast("handler", new ServerHandler());// 消息处理器
				}
			});
			// 配置连接属性
			bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
			bootstrap.childOption(ChannelOption.SO_LINGER, 0);
			bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
			bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.option(ChannelOption.SO_RCVBUF, 1024000);
			bootstrap.option(ChannelOption.SO_SNDBUF, 1024000);

			bootstrap.bind(host, port).sync().channel().closeFuture().sync();

		} catch (Exception e) {
			log.error("[startServer] error is:", e);
		} finally {
			log.info("[startServer] start Server finally,server stop");
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
}
