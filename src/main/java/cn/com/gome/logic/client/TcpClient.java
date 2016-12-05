package cn.com.gome.logic.client;

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
import java.net.InetSocketAddress;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.client.TCPProtocolDecoder;
import cn.com.gome.logic.client.TCPProtocolEncoder;
import cn.com.gome.logic.client.TcpClientHandler;
import cn.com.gome.logic.cache.ServerCache;
import cn.com.gome.logic.utils.IPUtils;

/**
 * netty tcp client
 */
public class TcpClient {
	static Logger log = LoggerFactory.getLogger(TcpClient.class);
	private final static Lock lock = new ReentrantLock(true);
	private static TcpClient instance = new TcpClient();
	private EventLoopGroup group = null;
	private Bootstrap b = null;
	private Channel channel = null;

	public synchronized static TcpClient getInstance() {
		return instance;
	}

	private TcpClient() {

	}

	private Channel getConnect(String respKey) {
		log.info("[getConnect] respKey=[{}]", respKey);
		try {
			lock.lock();
			channel = ServerCache.getRespChannel(respKey);
			if (channel != null) {
				if (channel.isActive() && channel.isWritable()) {
					return channel;
				} else {
					log.info("[getConnect] close channel, respKey=[{}]", respKey);
					ServerCache.removeRespChannel(respKey);
					this.close();
				}
			}
			String[] arr = respKey.split(":");
			if (arr.length != 2) {
				return null;
			}

			String host = IPUtils.longToIP(Long.valueOf(arr[0]));
			if ("0.0.0.0".equals(host)) {
				host = "127.0.0.1";
			}
			int port = Integer.valueOf(arr[1]);
			log.info("[getConnect] host=[{}],port=[{}]", host, port);
			group = new NioEventLoopGroup();
			b = new Bootstrap();
			b.group(group);
			b.channel(NioSocketChannel.class);
			b.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					pipeline.addLast(new TCPProtocolDecoder());// 解码器
					pipeline.addLast(new TCPProtocolEncoder());// 编码器
					pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 0, Global.HEARTBEAT_TIME - 5));// 定时器,秒
					pipeline.addLast(new TcpClientHandler());
				}
			});
			b.option(ChannelOption.TCP_NODELAY, true);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			b.option(ChannelOption.SO_TIMEOUT, 1000);
			b.option(ChannelOption.SO_LINGER, 10);
			ChannelFuture f = b.connect(host, port).await();
			if (f.isSuccess()) {
				channel = f.channel();
				// 放入内存
				ServerCache.setRespChannel(respKey, channel);
				log.info("[getConnect] key=[{}]", respKey);
				return channel;
			} else {
				log.info("[getConnect] failed,key=[{}]", respKey);
				this.close();

				return null;
			}
		} catch (Exception e) {
			this.close();
			log.error("[getConnect] cause is:", e);
			return null;
		} finally {
			lock.unlock();
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
	public int sendMsg(String respKey, ProtocolPackage msg) {
		try {
			int tranceId = msg.getTraceId();
			log.info("[sendMsg] tranceId=[{}],respKey=[{}],receiveId=[{}]", tranceId, respKey, msg.getReceiveId());
			String[] arr = respKey.split(":");
			if (arr.length != 2) {
				log.error("[sendMsg] response info is error!tranceId=[{}]", tranceId);
				return -1;
			}
			String host = IPUtils.longToIP(Long.valueOf(arr[0]));
			int port = Integer.valueOf(arr[1]);
			log.info("[sendMsg] tranceId=[{}],host=[{}],port=[{}],receiveId=[{}]", tranceId, host, port,
					msg.getReceiveId());

			channel = ServerCache.getRespChannel(respKey);
			if (channel == null) {
				log.error("[sendMsg] channel is null,get new channel,tranceId=[{}],respKey=[{}],receiveId=[{}]",
						tranceId, respKey, msg.getReceiveId());
				channel = getConnect(respKey);
			}
			if (channel != null && channel.isActive() && channel.isOpen() && channel.isWritable()) {
				log.info("[sendMsg]tranceId=[{}],channel id=[{}]", tranceId, channel.id());
				InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
				String clientIp = address.getHostString();
				int clietPort = address.getPort();
				log.info("[sendMsg 2] tranceId=[{}],clientIp=[{}],clietPort=[{}],receiveId=[{}]", tranceId, clientIp,
						clietPort, msg.getReceiveId());
				channel.writeAndFlush(msg);// .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);

				return 0;
			}
			log.info("[sendMsg] send msg failed,channel is not active close channel,tranceId=[{}],respKey=[{}]",
					tranceId, respKey);
			ServerCache.removeRespChannel(respKey);
			this.close();
			// 重连，重发
			channel = getConnect(respKey);
			if (channel != null && channel.isActive() && channel.isOpen() && channel.isWritable()) {
				InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
				String clientIp = address.getHostString();
				int clietPort = address.getPort();
				log.info("[sendMsg 2] tranceId=[{}],clientIp=[{}],clietPort=[{}],receiveId=[{}]", tranceId, clientIp,
						clietPort, msg.getReceiveId());
				channel.writeAndFlush(msg);
			} else {
				log.error("[sendMsg] reConnect channel is null close channel,tranceId=[{}],respKey=[{}]", tranceId,
						respKey);
				this.close();

				return -1;
			}

			// if (channel != null) {
			// if (channel.isActive() && channel.isWritable()) {
			// InetSocketAddress address = (InetSocketAddress)
			// channel.remoteAddress();
			// String clientIp = address.getHostString();
			// int clietPort = address.getPort();
			// log.info("[sendMsg]
			// tranceId=[{}],clientIp=[{}],clietPort=[{}],receiveId=[{}]",
			// tranceId, clientIp,
			// clietPort, msg.getReceiveId());
			// channel.writeAndFlush(msg);//
			// .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			// } else {
			// log.info("[sendMsg] non writable close channel
			// tranceId=[{}],respKey=[{}]", tranceId, respKey);
			// ServerCache.removeRespChannel(respKey);
			// this.close();
			// }
			// }
		} catch (Exception e) {
			// this.close();
			log.error("[sendMsg]:", e);
			return -1;
		}

		return 0;
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
}
