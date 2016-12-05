package cn.com.gome.logic.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.service.ResourceReportService;

/**
 * 服务器资源quartz job
 */
public class ServerResourceJob implements Job {
	Logger log = LoggerFactory.getLogger(ServerResourceJob.class);
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.info("[execute]............");
		ResourceReportService service = new ResourceReportService();
		service.reportResource();
		//接入层列表赋值
		service.getGateWayServer();
		
//		Map<String, String> gateWayMap = service.getGateWayServer();
//		ServerCache.setGateWayMap(gateWayMap);
//		double cpuRate = ServerResourceUtils.getCupRate();
//		double memRate = ServerResourceUtils.getMemRate();
//		String serverIp = Global.SERVER_IP;
//		int serverPort = Global.SERVER_PORT;
//		ServerResource resource = new ServerResource();
//		resource.setServerIp(serverIp);
//		resource.setServerPort(serverPort);
//		resource.setCpuRate(cpuRate);
//		resource.setMemRate(memRate);
//		resource.setTime(System.currentTimeMillis());
//		Map<String, Channel> channelMap = ServerCache.getChannelMap();
//		List<Map<String, Object>> clients = new ArrayList<Map<String, Object>>();
//		for(String key : channelMap.keySet()) {
//			String[] keyArr = key.split(":");
//			String clientIp = IPUtils.longToIP(Long.valueOf(keyArr[0]));
//			int port = Integer.valueOf(keyArr[1]);
//			Map<String, Object> client = new HashMap<String, Object>();
//			client.put("ip", clientIp);
//			client.put("port", port);
//			clients.add(client);
//		}
//		resource.setClients(clients);
//		
//		Map<String, Channel> respChannelMap = ServerCache.getRespChannelMap();
//		List<Map<String, Object>> responses = new ArrayList<Map<String, Object>>();
//		for(String key : respChannelMap.keySet()) {
//			String[] keyArr = key.split(":");
//			String respIp = IPUtils.longToIP(Long.valueOf(keyArr[0]));
//			int port = Integer.valueOf(keyArr[1]);
//			Map<String, Object> response = new HashMap<String, Object>();
//			response.put("ip", respIp);
//			response.put("port", port);
//			responses.add(response);
//		}
//		resource.setResponses(responses);
//		ServerResurceRedisDao dao = new ServerResurceRedisDao();
//		dao.saveServerResource(serverType, resource);
	}
}
