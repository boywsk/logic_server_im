package cn.com.gome.logic.utils;

import java.net.InetSocketAddress;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.cache.ServerCache;
import cn.com.gome.logic.client.ConnectionManager;
//import cn.com.gome.logic.client.TcpClient;
import cn.com.gome.logic.utils.IPUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * 消息发送工具类
 */
public class TcpDataSendUtils {

	private final static Logger log = LoggerFactory.getLogger(TcpDataSendUtils.class);

	/**
	 * 发送tcp数据
	 * @param clientKey
	 * @param pack
	 */
	public static void sendTcpData(final String clientKey, ProtocolPackage pack) {
		log.info("[sendTcpData] traceId=[{}],clientKey=[{}],cmd=[{}],respIP is=[{}],respPort=[{}],receiveId=[{}]", 
				pack.getTraceId(), clientKey, pack.getCommand(), IPUtils.longToIP(pack.getRspIP()), pack.getRspPort(), pack.getReceiveId());
		final Map<String, String> GATEWAY_ADDRESS = Global.GATEWAY_ADDRESS;
		try {
			if (pack.getRspPort() > 0 && pack.getCommand() != Command.CMD_HEARTBEAT) {
				String key = pack.getRspIP() + ":" + pack.getRspPort();
				if(GATEWAY_ADDRESS != null) {
//					for(String gwKey : GATEWAY_ADDRESS.keySet()) {
//						log.info("[sendTcpData] key=[{}],gwKey=[{}]", key, gwKey);
//					}
					String value = GATEWAY_ADDRESS.get(key);
					log.error("[sendTcpData gateWay value] traceId=[{}],key=[{}],value=[{}]", pack.getTraceId(), key, value);
					if(!GATEWAY_ADDRESS.containsKey(key)) {
						log.error("[sendTcpData gateWay gateWay non-existent] traceId=[{}],key=[{}],"
								+ "receiveId=[{}],clientId=[{}]", pack.getTraceId(), key, pack.getReceiveId(), pack.getClientId());
						
						return;
					}
				} else {
					log.error("GATEWAY_ADDRESS is null");
				}
				log.info("[sendTcpData] traceId=[{}],key=[{}],receiveId=[{}],ack=[{}]", pack.getTraceId(), key, pack.getReceiveId(), pack.getAck());
//				TcpClient.getInstance().sendMsg(key, pack);
				ConnectionManager.getInstance().sendMsg(key, pack);
			} else {
				//心跳
				if(pack.getCommand() == Command.CMD_HEARTBEAT) {
					Channel client = ServerCache.getChannel(clientKey);
					if (client != null && client.isWritable() && client.isActive()) {
						InetSocketAddress address = (InetSocketAddress) client.remoteAddress();
						String clientIp = address.getHostString();
						int port = address.getPort();
						log.info("[sendTcpData] traceId=[{}],clientIp=[{}],port=[{}],uid=[{}]", pack.getTraceId(), clientIp, port, pack.getUid());
						client.writeAndFlush(pack).addListener(new ChannelFutureListener() {
							@Override
							public void operationComplete(ChannelFuture f) {
								if (f.isSuccess()) {
									log.info("[sendTcpData] to gateway success,traceId=[{}],uid=[{}]", pack.getTraceId(), pack.getUid());
									log.info("[sendTcpData] clientIp=[{}],port=[{}],traceId=[{}],uid=[{}]", pack.getTraceId(), clientIp, port, pack.getUid());
								} else {
									log.info("[sendTcpData] to gateway failed,traceId=[{}],uid=[{}]", pack.getTraceId(), pack.getUid());
									ServerCache.removeChannel(clientKey);
								}
							}
						});
					} else {
						log.error("[sendTcpData] cause is get channel is null fortraceId=[{}], key=[{}]", pack.getTraceId(), clientKey);
						ServerCache.removeChannel(clientKey);
					}
				}
			}
		} catch(Exception e) {
			log.error("[sendTcpData]", e);
		}
		
		
//			else {
//				String appId = pack.getAppId();
//				long uid = pack.getUid();
//				long receiveId = pack.getReceiveId();
//				byte clientId = pack.getClientId();
//				log.info("[sendTcpData] appId=[{}],uid=[{}],clientId=[{}], clientId=[{}]", appId, uid, clientId, clientId);
//				UserRedisDao userRedisDao = new UserRedisDao();
//				Map<String, String> map = userRedisDao.listUserRsp(appId, uid);
//				if (map != null && !map.isEmpty()) {
//					for (String key : map.keySet()) {
//						String[] keyArr = key.split("_");
//						if (keyArr.length < 2) {
//							log.info("[sendTcpData]=========redis cache is error==========uid=[{}]", uid);
//							continue;
//						}
//						byte bClientId = Integer.valueOf(keyArr[0]).byteValue();
//						log.info("[sendTcpData] uid=[{}],key=[{}],clientId=[{}]", uid, key, bClientId);
//						if(uid == receiveId && bClientId == clientId) {
//							log.info("[sendTcpData self] uid=[{}],clientId=[{}],bclientId=[{}]", uid, clientId, bClientId);
//							String clientvalue = map.get(key);
//							String[] rspArr = clientvalue.split(":");
//							String host = IPUtils.longToIP(Long.valueOf(rspArr[0]));
//							int port = Integer.parseInt(rspArr[1]);
//							log.info("[sendTcpData] host=[{}],port=[{}],uid=[{}],ack=[{}]", host, port, uid, pack.getAck());
//							if(GATEWAY_ADDRESS != null) {
//								if(!GATEWAY_ADDRESS.containsKey(clientvalue)) {
//									log.error("[sendTcpData gateWay is errod 2] key=[{}],uid=[{}]", key, pack.getUid());
//									return;
//								}
//							}
//							TcpClient.getInstance().sendMsg(clientvalue, pack);
//							break;
//						}
//					}
//				} else {
//					log.info("[sendTcpData]=========gata way null==========uid=[{}]", uid);
//				}
//			}
//			Channel client = ServerCache.getChannel(clientKey);
//			if (client != null && client.isWritable() && client.isActive()) {
//				InetSocketAddress address = (InetSocketAddress) client.remoteAddress();
//				String clientIp = address.getHostString();
//				int port = address.getPort();
//				log.info("[sendTcpData] clientIp=[{}],port=[{}],uid=[{}]", clientIp, port, pack.getUid());
				
				
				
//				client.writeAndFlush(pack).addListeners(listeners);
//				client.writeAndFlush(pack).addListener(new ChannelFutureListener() {
//					@Override
//					public void operationComplete(ChannelFuture f) {
//						if (f.isSuccess()) {
//							log.info("[sendTcpData] to gateway success,uid=[{}]", pack.getUid());
//							log.info("[sendTcpData] clientIp=[{}],port=[{}],uid=[{}]", clientIp, port, pack.getUid());
//						} else {
//							log.info("[sendTcpData] to gateway failed,uid=[{}]", pack.getUid());
//							ServerCache.removeChannel(clientKey);
//						}
//					}
//				});
//			} else {
//				log.error("[sendTcpData] cause is get channel is null for key=[{}]", clientKey);
//				ServerCache.removeChannel(clientKey);
//			}
	}
}
