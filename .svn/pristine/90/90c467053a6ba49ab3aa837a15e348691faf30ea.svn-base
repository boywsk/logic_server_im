package cn.com.gome.logic.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;

import cn.com.gome.logic.global.Global;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class UdpRequestUtils {
	private static Logger log = LoggerFactory.getLogger(UdpRequestUtils.class);
	private final List<String> hosts = Global.DISPATCHER_ADDRESS;
	
	public String SendMsg(String msg) {
		String receiveMsg = "";
		try {
			if (Strings.isNullOrEmpty(msg)) {
				log.error("msg:{} is empty!", msg);
				return null;
			}
			
			int size = hosts.size();;
			if(size <= 0) {
				log.error("[sendReport] dispatcher is empty！！！size=[{}]", size);
				return "";
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

			InetAddress address = InetAddress.getByName(ip); // 服务器地址
																			// 10.125.3.61
																			// 10.69.16.92
			//"10.125.72.89"
//			int port = 8877; // 服务器的端口号 国美+ 8877 企业办公 8866
			// 创建发送方的数据报信息
			DatagramPacket dataGramPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, port);

			DatagramSocket socket = new DatagramSocket(); // 创建套接字
			socket.setSoTimeout(3000);
			socket.send(dataGramPacket); // 通过套接字发送数据

			// 接收服务器反馈数据
			byte[] buf = new byte[2048];
			DatagramPacket backPacket = new DatagramPacket(buf, buf.length);
			socket.receive(backPacket); // 接收返回数据
			receiveMsg = new String(buf, 0, backPacket.getLength());
			log.info("UDP服务器返回的数据为:" + receiveMsg);
			socket.close();
		} catch (IOException e) {
			// e.printStackTrace();
			log.error("error:{}", e);
		}
		return receiveMsg;
	}

	public static void main(String[] args) {
		String msg = new UdpRequestUtils().SendMsg("{\"requestType\": 2,\"reqServersMsg\": {\"type\": 1}}");
		JSONObject obj = JSON.parseObject(msg);
		JSONArray obj2 = obj.getJSONArray("rspServers");
		for (Object obj3 : obj2) {
			JSONObject obj4 = (JSONObject) obj3;
			JSONArray obj5 = obj4.getJSONArray("ipPort");
			int size = obj5.size();
			for(int i = 0; i < size; i++) {
				String str = obj5.getString(i);
				System.out.println("str:" + str);
			}
		}

		System.out.println("UDP服务器返回结果:" + msg);
	}
}
