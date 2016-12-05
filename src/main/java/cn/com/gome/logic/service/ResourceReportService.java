package cn.com.gome.logic.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;

import cn.com.gome.logic.cache.ServerCache;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.utils.IPUtils;
import cn.com.gome.logic.utils.UdpReportUtils;
import cn.com.gome.logic.utils.UdpRequestUtils;

/**
 * 服务器资源上报
 */
public class ResourceReportService {
	Logger log = LoggerFactory.getLogger(ResourceReportService.class);
	
	/**
	 * 服务资源上报到GK上
	 */
	public void reportResource() {
		Set<Short> cmds = ServerCache.listCmd();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("requestType", Constant.REQUEST_TYPE.REPORT.value);
		Map<String, Object> map = new HashMap<String, Object>();
		String ipPort = Global.SERVER_IP + ":" + Global.SERVER_PORT;
		map.put("type", Constant.SERVER_TYPE.LOGIC.value);
		map.put("ipPort", ipPort);
		map.put("cmd", cmds);
		data.put("reqReportMsg", map);
		String str = JSON.toJSONString(data);
		log.info("[reportResource] data=[{}]", str);
		UdpReportUtils reportUtils = new UdpReportUtils();
		reportUtils.sendReport(str);
	}
	
	/**
	 * 获取接入层
	 * @return
	 */
	public Map<String, String> getGateWayServer() {
		Map<String, String> map = new ConcurrentHashMap<String, String>();
		String msg = new UdpRequestUtils().SendMsg("{\"requestType\": 2,\"reqServersMsg\": {\"type\": 1}}");
		if(Strings.isNullOrEmpty(msg)) {
			return map;
		}
		JSONObject obj = JSON.parseObject(msg);
		if(obj.containsKey("rspServers")) {
			JSONArray rspServers = obj.getJSONArray("rspServers");
			if(rspServers != null && !rspServers.isEmpty()) {
				for (Object rspServer : rspServers) {
					JSONObject rspServerObj = (JSONObject) rspServer;
					if(rspServerObj != null && !rspServerObj.isEmpty()) {
						if(rspServerObj.containsKey("ipPort")) {
							JSONArray ipPorts = rspServerObj.getJSONArray("ipPort");
							if(ipPorts != null && !ipPorts.isEmpty()) {
								int size = ipPorts.size();
								for(int i = 0; i < size; i++) {
									String address = ipPorts.getString(i);
									String[] arrAtr = address.split(":");
									long lip = IPUtils.ipToLong(arrAtr[0]);
									String key =  lip + ":" + arrAtr[1];
									log.info("address=[{}],key=[{}]", address, key);
									map.put(key, "");
								}
							}
						}
					}
				}
			}
		}
		//赋值
		Global.GATEWAY_ADDRESS = map;
//		for(String key : map.keySet()) {
//			Global.GATEWAY_ADDRESS.put(key, "");
//		}
//		for(String key : Global.GATEWAY_ADDRESS.keySet()) {
//			if(!map.containsKey(key)) {
//				Global.GATEWAY_ADDRESS.remove(key);
//			}
//		}
		
		return map;
	}
}
