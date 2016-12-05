package cn.com.gome.logic.utils;

import java.net.InetSocketAddress;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;
import cn.com.gome.logic.global.Global;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * repost udp客戶端
 */
public class UdpReportUtils {
	private static final Logger log = LoggerFactory.getLogger(UdpReportUtils.class);
	private final List<String> hosts = Global.DISPATCHER_ADDRESS;
	
	/**
	 * 发送数据
	 * @param PbGskReport
	 * @param serverIp
	 */
	public void sendReport(String data) {
		log.debug("[sendReport] report data=[{}]", data);
		Bootstrap bootstrap = null;
		Channel channel = null;
		
		int size = hosts.size();;
		if(size <= 0) {
			log.error("[sendReport] dispatcher is empty！！！size=[{}]", size);
			return;
		}
		int random = StringUtils.getRandomInt(size);
		String str = hosts.get(random);
		String[] hostArr = str.split(":");
		if(hostArr.length < 2) {
			log.error("[sendReport] dispatcher config error！！！");
		}
		String ip = hostArr[0];
		int port = Integer.parseInt(hostArr[1]);
		log.info("[sendReport] report dispatcher server ip=[{}],port=[{}]", ip, port);
		if(Strings.isNullOrEmpty(data)) {
			log.error("[sendReport] send data is null or empty!!!!!");
			return;
		}
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			bootstrap = new Bootstrap();
			bootstrap.group(group)
			.channel(NioDatagramChannel.class)
			.option(ChannelOption.SO_SNDBUF, 2621440)
			.handler(new UDPClientHandler());
			channel = bootstrap.bind(0).sync().channel();
			byte[] dataByte = data.getBytes("UTF-8");
			ByteBuf buf = Unpooled.buffer(dataByte.length);
			buf.writeBytes(dataByte);
			InetSocketAddress address = new InetSocketAddress(ip, port);
			ChannelFuture f = channel.writeAndFlush(new DatagramPacket(buf, address)).sync();
			if(f.isSuccess()) {
				log.info("[sendReport] success!!!");
			}
		} catch (Exception e) {
			log.error("[sendReport]:", e);
		} finally {
			if(channel != null) {
				channel.close();
				channel = null;
			}
			bootstrap = null;
			group.shutdownGracefully();
		}
	}
	
	@Sharable
	private class UDPClientHandler extends ChannelHandlerAdapter {
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			log.error("[exceptionCaught]:", cause);
			ctx.close();
		}
	}
}
