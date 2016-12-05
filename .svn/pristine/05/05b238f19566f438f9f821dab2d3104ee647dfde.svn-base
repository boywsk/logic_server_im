package cn.com.gome.logic.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

/**
 * tcp client
 */
public class Connection {
	Logger log = LoggerFactory.getLogger(Connection.class);
	private long createTime;
	private EventLoopGroup group;
	private Bootstrap b;
	private Channel channel;
	
	public Connection() {
		
	}
	
	public Connection(EventLoopGroup group, Bootstrap b, Channel channel) {
		this.group = group;
		this.b = b;
		this.channel = channel;
		this.createTime = System.currentTimeMillis();
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public EventLoopGroup getGroup() {
		return group;
	}

	public void setGroup(EventLoopGroup group) {
		this.group = group;
	}

	public Bootstrap getB() {
		return b;
	}

	public void setB(Bootstrap b) {
		this.b = b;
	}
	
	public void close() {
		log.info("[close] close channel!!!");
		if (channel != null) {
			channel.close();
			channel = null;
		}
		if (group != null) {
			group.shutdownGracefully();
			group = null;
		}
		b = null;
	}

	// private final static Lock lock = new ReentrantLock(true);
	// private EventLoopGroup group = null;
	// private Bootstrap b = null;
	// private Channel channel = null;
	// /**
	// * 获取连接
	// * @param ip
	// * @param port
	// * @return
	// */
	// public Channel getConnect(String ip, int port) {
	// log.info("[getConnect] ip=[{}],port=[{}]", ip, port);
	// try {
	// lock.lock();
	// if (channel != null) {
	// if (channel.isActive() && channel.isWritable()) {
	// log.info("[getConnect] channel exist!!!, channel id=[{}]", channel.id());
	// return channel;
	// }
	// }
	// group = new NioEventLoopGroup();
	// b = new Bootstrap();
	// b.group(group);
	// b.channel(NioSocketChannel.class);
	// b.option(ChannelOption.TCP_NODELAY, true);
	// b.option(ChannelOption.SO_KEEPALIVE, true);
	// b.handler(new ChannelInitializer<SocketChannel>() {
	// @Override
	// protected void initChannel(SocketChannel ch) throws Exception {
	// ChannelPipeline pipeline = ch.pipeline();
	// pipeline.addLast(new TCPProtocolDecoder());// 解码器
	// pipeline.addLast(new TCPProtocolEncoder());// 编码器
	// pipeline.addLast("idleStateHandler", new IdleStateHandler(0,
	// Global.HEARTBEAT_TIME - 10, 0));// 定时器,秒
	// pipeline.addLast(new ConnectionHandler());
	// }
	// });
	// ChannelFuture f = b.connect(ip, port).await();
	// if (f.isSuccess()) {
	// channel = f.channel();
	// log.info("[getConnect] channel id=[{}]", channel.id());
	// return channel;
	// }
	// } catch (Exception e) {
	// log.error("[getConnect] cause is:", e);
	// } finally {
	// lock.unlock();
	// }
	//
	// return channel;
	// }
	//
	// public void close() {
	// log.info("[close] close channel!!!");
	// if (channel != null) {
	// channel.close();
	// channel = null;
	// }
	// b = null;
	// if (group != null) {
	// group.shutdownGracefully();
	// group = null;
	// }
	// }
}
