package cn.com.gome.logic.quartz;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.cache.ServerCache;
import cn.com.gome.logic.client.Connection;
import cn.com.gome.logic.client.ConnectionManager;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.utils.IPUtils;
import io.netty.channel.ChannelId;

/**
 * 检车客户端连接quartz job
 */
public class CheckConnectionJob implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		Logger log = LoggerFactory.getLogger(CheckConnectionJob.class);
		Map<String, ConcurrentHashMap<ChannelId, Connection>> pool = ServerCache.getConnectionPool();
		log.info("[execute] poll size=[{}]", pool.size());
		for(String respKey : pool.keySet()) {
			String[] arr = respKey.split(":");
			if(arr.length < 2) {
				log.error("[execute] rspKey is error!!! rspKey=[{}]", respKey);
				continue;
			}
			String ip = IPUtils.longToIP(Long.valueOf(arr[0]));
			int port = Integer.valueOf(arr[1]);
			log.info("[execute] ip=[{}],port=[{}]", ip, port);
			ConcurrentHashMap<ChannelId, Connection> connections = pool.get(respKey);
			int connectionSize = connections == null ? 0 : connections.size();
			log.info("[execute]  ip=[{}],port=[{}],connection size=[{}]", ip, port, connectionSize);
			if(connectionSize < Global.RESPONSE_POOL_SIZE) {
				int subSize = Global.RESPONSE_POOL_SIZE - connectionSize;
				log.info("[execute]  ip=[{}],port=[{}],subSize=[{}]", ip, port, subSize);
				ConnectionManager.getInstance().initPool(respKey, subSize);
			}
		}
	}
}
