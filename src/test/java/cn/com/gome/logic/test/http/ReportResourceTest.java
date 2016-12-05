package cn.com.gome.logic.test.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.JSON;

import cn.com.gome.logic.cache.ServerCache;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.model.ServerResource;
import cn.com.gome.logic.utils.HttpUtils;
import cn.com.gome.logic.utils.IPUtils;
import cn.com.gome.logic.utils.ServerResourceUtils;
import io.netty.channel.Channel;


public class ReportResourceTest {

	public static void main(String[] args) throws Exception {
//		String url = "http://10.69.16.56:8844/reportServerRes";
		String url = "http://10.125.3.41:8844/reportServerRes";
//		ServerResource server = new ServerResource();
////		server.setTypeKey("LOGIC_SERVER_DEVEL");
//		server.setServerIp("10.125.3.41");
//		server.setServerPort(8080);
//
//		List<Map<String, Object>> clients = new ArrayList<Map<String, Object>>();
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("ip", "10.69.16.56");
//		map.put("port", 8000);
//		clients.add(map);
//		clients.add(map);
//		server.setClients(clients);
//		
//		List<Map<String, Object>> responses = new ArrayList<Map<String, Object>>();
//		responses.add(map);
//		server.setResponses(responses);
//		server.setInConnNum(2);
//		server.setOutConnNum(5);
//		server.setCpuRate(11.34);
//		server.setMemRate(56.19);
//		server.setServerType(Constant.SERVER_TYPE.LOGIC.value);
////		server.setReportTime(System.currentTimeMillis());
////		Map<String, Object> appCounter = new HashMap<String, Object>();
////		appCounter.put("app_id_1", 10);
////		appCounter.put("app_id_2", 12);
////		server.setAppCounter(appCounter);
//		
//		String data = JSON.toJSONString(server);
		
		
		double cpuRate = ServerResourceUtils.getCupRate();
		double memRate = ServerResourceUtils.getMemRate();
		String serverIp = Global.SERVER_IP;
		int serverPort = Global.SERVER_PORT;
		ServerResource server = new ServerResource();
		server.setServerType(Constant.SERVER_TYPE.LOGIC.value);
		server.setServerIp(serverIp);
		server.setServerPort(serverPort);
		server.setCpuRate(cpuRate);
		server.setMemRate(memRate);
		//server.setTime(System.currentTimeMillis());
		Map<String, Channel> channelMap = ServerCache.getChannelMap();
		List<Map<String, Object>> clients = new ArrayList<Map<String, Object>>();
		for(String key : channelMap.keySet()) {
			String[] keyArr = key.split(":");
			String clientIp = IPUtils.longToIP(Long.valueOf(keyArr[0]));
			int port = Integer.valueOf(keyArr[1]);
			Map<String, Object> client = new HashMap<String, Object>();
			client.put("ip", clientIp);
			client.put("port", port);
			clients.add(client);
		}
		server.setClients(clients);
		
		Map<String, Channel> respChannelMap = ServerCache.getRespChannelMap();
		List<Map<String, Object>> responses = new ArrayList<Map<String, Object>>();
		for(String key : respChannelMap.keySet()) {
			String[] keyArr = key.split(":");
			String respIp = IPUtils.longToIP(Long.valueOf(keyArr[0]));
			int port = Integer.valueOf(keyArr[1]);
			Map<String, Object> response = new HashMap<String, Object>();
			response.put("ip", respIp);
			response.put("port", port);
			responses.add(response);
		}
		server.setResponses(responses);
		server.setInConnNum(clients.size());
		server.setOutConnNum(responses.size());
		
		
		//暂时保存redis======================================
//		final String serverType = Global.SERVER_RESOURCE;
//		ServerResurceRedisDao dao = new ServerResurceRedisDao();
//		dao.saveServerResource(serverType, resource);
		
		String data = JSON.toJSONString(server);
		
		System.out.println(data);
		String str = HttpUtils.sendRequest(url, null, "application/json", data);
		System.out.println(str);

	}

}
