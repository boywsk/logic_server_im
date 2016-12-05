package cn.com.gome.logic.zookeeper;

import java.util.List;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.global.Global;

/**
 *  实现zookeeper数据配置和管理功能
 */
public class ZKMananger {
	static Logger log = LoggerFactory.getLogger(ZKMananger.class);
	private static ZKMananger manager;
	private static ZkClient zkClient = null;
	
	static {
		zkClient = new ZkClient(Global.ZOOKEEPER_ADDRESS, 3000);
	}
	
	private ZKMananger() {
		
	}
	
	public static ZKMananger getInstance() {
		if(manager == null) {
			manager = new ZKMananger();
		}
		
		return manager;
	}
	
	/**
	 * 获取节点下的子节点
	 * @param path
	 * @return
	 */
	public List<String> getChildren(String path) {
		log.info("[getChildren] path=[{}]", path);
		return zkClient.getChildren(path);
	}
	
}
