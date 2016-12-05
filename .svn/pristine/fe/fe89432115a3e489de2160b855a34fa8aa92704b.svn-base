package cn.com.gome.logic.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.cache.ServerCache;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.utils.IPUtils;
import cn.com.gome.logic.utils.StringUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * tcp客户端连接管理
 */
public class ConnectionManager {
	static Logger log = LoggerFactory.getLogger(ConnectionManager.class);

	private final static Lock lock = new ReentrantLock(true);
	final Map<String, String> GATEWAY_ADDRESS = Global.GATEWAY_ADDRESS;
	private static final int poolSize = Global.RESPONSE_POOL_SIZE;
	private static ConnectionManager instance = new ConnectionManager();

	private ConnectionManager() {

	}

	public synchronized static ConnectionManager getInstance() {
		return instance;
	}

	public void initPool(String respKey, int size) {
		log.info("[initPool] respKey=[{}],size=[{}]", respKey, size);
		try {
			lock.lock();
			String[] arr = respKey.split(":");
			if (arr.length != 2) {
				log.error("[initPool] respKey info is error!!!");
			}
			String ip = IPUtils.longToIP(Long.valueOf(arr[0]));
			int port = Integer.valueOf(arr[1]);
			log.info("[initPool] ip=[{}],port=[{}]", ip, port);
			if (size > 0) {
				for (int i = 0; i < size; i++) {
					EventLoopGroup group = null;
					Bootstrap b = null;
					try {
						group = new NioEventLoopGroup();
						b = new Bootstrap();
						b.group(group);
						b.channel(NioSocketChannel.class);
						b.option(ChannelOption.TCP_NODELAY, true);
						b.option(ChannelOption.SO_KEEPALIVE, true);
						b.option(ChannelOption.SO_RCVBUF, 1024000);
						b.option(ChannelOption.SO_SNDBUF, 1024000);
						b.option(ChannelOption.SO_TIMEOUT, 1000);
						b.handler(new ChannelInitializer<SocketChannel>() {
							@Override
							protected void initChannel(SocketChannel ch) throws Exception {
								ChannelPipeline pipeline = ch.pipeline();
								pipeline.addLast(new TCPProtocolDecoder());// 解码器
								pipeline.addLast(new TCPProtocolEncoder());// 编码器
								pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 0, Global.HEARTBEAT_TIME - 10));// 定时器,秒
								pipeline.addLast(new ConnectionHandler());
							}
						});
						ChannelFuture f = b.connect(ip, port).await();
						if (f.isSuccess()) {
							Channel channel = f.channel();
							log.info("[initPool] channel id=[{}]", channel.id());
							Connection connection = new Connection(group, b, channel);
							ServerCache.setConnection(respKey, connection);
						} else {
							b = null;
							if (group != null) {
								group.shutdownGracefully();
								group = null;
							}
							log.error("[initPool] can not connect;ip=[{}],port=[{}]",ip, port);
							Global.GATEWAY_ADDRESS.remove(respKey);
							break;
						}
					} catch (Exception e) {
						log.error("[initPool] cause is:", e);
						b = null;
						if (group != null) {
							group.shutdownGracefully();
							group = null;
						}
						Global.GATEWAY_ADDRESS.remove(respKey);
					}
				}	
			}
		} catch (Exception e) {
			log.error("[initPool] cause is:", e);
		} finally {
			lock.unlock();
		}
	}


	/**
	 * 获取channel
	 * @param respKey
	 * @return
	 */
	public Connection getConnection(String respKey) {
		log.info("[getConnection] respKey=[{}]", respKey);
		ConcurrentHashMap<ChannelId, Connection> channels = ServerCache.getConnections(respKey);
		if (channels == null || channels.isEmpty()) {
			initPool(respKey, poolSize);
		}
		channels = ServerCache.getConnections(respKey);
		if (channels != null && !channels.isEmpty()) {
//			List<Connection> list = (List<Connection>)channels.values();
			List<Connection> list = new ArrayList<Connection>(channels.values());
			int size = channels.size();
			int rand = StringUtils.getRandomInt(size);
			log.info("[getConnection] respKey=[{}],size=[{}],rand=[{}]", respKey, size, rand);
			Connection connection = list.get(rand);
			return connection;
//			for (Connection connection : channels.values()) {
//				if (i == rand) {
//					return connection;
//				}
//				i++;
//			}
		}
		return null;
	}

	/**
	 * 通过Channel发送消息
	 * @param clientKey
	 *            连接标识，格式：ip:port；ip是long类型
	 * @param msg
	 *            消息内容
	 * @return 0：发送成功，-1：发送失败
	 */
	public int sendMsg(String respKey, ProtocolPackage msg) {
		try {
			long tranceId = msg.getTraceId();
			long receiveId = msg.getReceiveId();
			byte clientId = msg.getClientId();
			byte ack = msg.getAck();
			log.info("[sendMsg] tranceId=[{}],respKey=[{}],receiveId=[{}],clientId=[{}],ack=[{}]", tranceId, respKey,
					receiveId, clientId, ack);
			String[] arr = respKey.split(":");
			if (arr.length != 2) {
				log.error("[sendMsg] response info is error!tranceId=[{}],receiveId=[{}],clientId=[{}],ack=[{}]",
						tranceId, receiveId, clientId, ack);
				return -1;
			}
			String host = IPUtils.longToIP(Long.valueOf(arr[0]));
			int port = Integer.valueOf(arr[1]);
			log.info("[sendMsg] tranceId=[{}],host=[{}],port=[{}],receiveId=[{}],clientId=[{}],ack=[{}]", tranceId,
					host, port, receiveId, clientId, ack);
			Connection connection = this.getConnection(respKey);
			if (connection == null) {
				log.error("[sendMsg] channel is null,get new channel,tranceId=[{}],respKey=[{}]", tranceId, respKey);
				connection = this.getConnection(respKey);
			}
			if(connection == null) {
				log.error("[sendMsg] get connection error!!! tranceId=[{}],respKey=[{}]", tranceId, respKey);
			}
			Channel channel = connection.getChannel();
			log.info("[sendMsg] tranceId=[{}],channel id=[{}]", tranceId, channel.id());
			if (channel != null && channel.isActive() && channel.isWritable()) {
				log.info("[sendMsg]tranceId=[{}],channel id=[{}]", tranceId, channel.id());
				channel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
					public void operationComplete(ChannelFuture future) throws Exception {
						if (future.isSuccess()) {
							log.info("[sendMsg] send msg success,tranceId=[{}],channel id=[{}]", tranceId, channel.id());
						} else {
							log.error("[sendMsg] send msg fail,tranceId=[{}],channel id=[{}]", tranceId, channel.id());
						}
					}
				});

				return 0;
			}
			log.info("[sendMsg] send msg failed,channel is not active close channel,tranceId=[{}],respKey=[{}]",
					tranceId, respKey);
			ServerCache.removeConnection(respKey, connection);
			connection.close();
			
			return -1;
		} catch (Exception e) {
			log.error("[sendMsg]:", e);
			return -1;
		}
	}
}