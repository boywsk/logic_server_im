package cn.com.gome.logic.test.tcp.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.cache.ServerCache;
import cn.com.gome.logic.client.TCPProtocolDecoder;
import cn.com.gome.logic.client.TCPProtocolEncoder;
import cn.com.gome.logic.client.TcpClientHandler;
import cn.com.gome.logic.utils.IPUtils;

/**
 * netty tcp client
 */
public class TcpClient {
	static Logger log = LoggerFactory.getLogger(TcpClient.class);

	private static TcpClient instance = new TcpClient();

	public synchronized static TcpClient getInstance() {
		return instance;
	}

	private TcpClient() {

	}

	private Channel getConnect(String respKey) {
		log.info("start...,respKey = [{}]", respKey);
		String[] arr = respKey.split(":");
		if (arr.length != 2) {
			return null;
		}
		String host = IPUtils.longToIP(Long.valueOf(arr[0]));
		if ("0.0.0.0".equals(host)) {
			host = "127.0.0.1";
		}
		int port = Integer.valueOf(arr[1]);

		log.info("host = [{}],port = [{}]", host, port);

		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.SO_KEEPALIVE, true)
					// .option(ChannelOption.SO_TIMEOUT, 3000)
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
			ChannelFuture f = b.connect(host, port).await();
			if (f.isSuccess()) {
				Channel channel = f.channel();
				// 放入内存
				ServerCache.setRespChannel(respKey, channel);
				log.info("end...,key = [{}]", respKey);
				return channel;
			} else {
				log.info("failed,key = [{}]", respKey);
				return null;
			}
		} catch (Exception e) {
			log.error("cause is:", e);
			return null;
		}
	}

	/**
	 * 通过Channel发送消息
	 * 
	 * @param clientKey
	 *            连接标识，格式：ip:port；ip是long类型
	 * @param msg
	 *            消息内容
	 * @return 0：发送成功，-1：发送失败
	 */
	public int sendMsg(String respKey, Object msg) {
		log.info("start...,respKey = [{}]", respKey);
		Channel channel = ServerCache.getRespChannel(respKey);
		if (channel == null) {
			log.error("channel is null,get new channel,respKey = [{}]", respKey);
			channel = getConnect(respKey);
		}
		if (channel != null && channel.isActive()) {
			channel.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
		} else {
			log.info("send msg failed,channel is not active,respKey = [{}]", respKey);
			return -1;
		}
		log.info("end...,respKey = [{}]", respKey);
		return 0;
	}
}
