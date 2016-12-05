package cn.com.gome.logic.utils;

import java.math.BigDecimal;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务器资源获取工具类
 */
public class ServerResourceUtils {
	static Logger log = LoggerFactory.getLogger(ServerResourceUtils.class);
	
	/**
	 * cpu使用率
	 * @return
	 */
	public static double getCupRate() {
		Sigar sigar = new Sigar();
		try {
			CpuPerc[] cpuList = sigar.getCpuPercList();
			double cpuRate = 0.0;
			for(CpuPerc cpu : cpuList) {
				double d = cpu.getCombined();
				cpuRate += d;
			}
			cpuRate = cpuRate / (double)cpuList.length * 100;
			BigDecimal b = new BigDecimal(cpuRate);
			double CPURate = b.setScale(2, BigDecimal.ROUND_CEILING).doubleValue();  
			return CPURate;
		} catch (Exception e) {
			log.error("[getCupRate]:", e);
		}
		
		return -0.0;
	}
	
	/**
	 * 内存使用率
	 * @return
	 */
	public static double getMemRate() {
		Sigar sigar = new Sigar();
		try {
			Mem mem = sigar.getMem();
			double d = mem.getUsedPercent();
			BigDecimal b = new BigDecimal(d);
			double rate = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

			return rate;
		} catch (Exception e) {
			log.error("[getCupRate]:", e);
		}
		
		return -0.0;
	}
}
