package cn.com.gome.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.mq.ConsultMQRecver;
import cn.com.gome.logic.mq.MQRecver;
import cn.com.gome.logic.quartz.QuartzStart;
import cn.com.gome.logic.server.LogicServer;
import cn.com.gome.logic.service.ResourceReportService;
import cn.com.gome.logic.utils.ClassLoadUtils;
import cn.com.gome.logic.utils.StringUtils;

public class Main {
	static Logger log = LoggerFactory.getLogger(Main.class);

	static {
		log.info("[static] config file base path {}", StringUtils.getRealPath());
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(lc);
		lc.reset();
		try {
			configurator.doConfigure(StringUtils.getRealPath() + "/config/logback.xml");
		} catch (JoranException e) {
			log.error("[static] error is:", e);
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
	}

	public static void main(String[] args) {
		new Thread() {
			public void run() {
				ClassLoadUtils.loadClass(Global.HANDLER_CLASSPATH);
				//启动上报能力集
				ResourceReportService service = new ResourceReportService();
				service.reportResource();
				//接入层列表赋值
				service.getGateWayServer();
				
				int process = Runtime.getRuntime().availableProcessors();
				// 启动服务器
				new LogicServer(process * 2).startServer(Global.SERVER_IP, Global.SERVER_PORT);
			};
		}.start();
		
		// 启动mq
		new Thread() {
			public void run() {
				try {
					MQRecver.getInstance().init();
				} catch (Exception e) {
					log.info("MQRecver init error:", e);
				}
			};
		}.start();
		// 启动客服咨询mq
		new Thread() {
			public void run() {
				try {
					ConsultMQRecver.getInstance().init();
				} catch (Exception e) {
					log.info("MQRecver init error:", e);
				}
			};
		}.start();
		
		// 启动定时汇报
		QuartzStart.start();
	}
}
