package cn.com.gome.logic.quartz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.global.Global;

/**
 * quartz启动入口
 */
public class QuartzStart {
	static Logger log = LoggerFactory.getLogger(QuartzStart.class);
	
	public static void start() {
		log.info("quartz start!");
		ServerResourceJob resJob = new ServerResourceJob();
		String time = Global.SERVER_RESOURCE_EXPIRE;
		QuartzManager.addJob("jobTest", resJob, time);//0 0/1 * * * ?
		
		CheckConnectionJob checkJob = new CheckConnectionJob();
		QuartzManager.addJob("checkJob", checkJob, time);
	}
}
