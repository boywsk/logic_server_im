package cn.com.gome.logic.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import cn.com.gome.logic.global.Constant;

/**
 * 服务器资源
 */
public class ServerResource implements Serializable {
	private static final long serialVersionUID = 1L;

	private int serverType = Constant.SERVER_TYPE.LOGIC.value;
	private String serverIp;
	private int serverPort;
	private double cpuRate;
	private double memRate;
	private long inConnNum;
	private long outConnNum;
	private long time;
	private List<Map<String, Object>> clients;
	private List<Map<String, Object>> responses;

	public int getServerType() {
		return serverType;
	}

	public void setServerType(int serverType) {
		this.serverType = serverType;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public double getCpuRate() {
		return cpuRate;
	}

	public void setCpuRate(double cpuRate) {
		this.cpuRate = cpuRate;
	}

	public double getMemRate() {
		return memRate;
	}

	public void setMemRate(double memRate) {
		this.memRate = memRate;
	}

	public long getInConnNum() {
		return inConnNum;
	}

	public void setInConnNum(long inConnNum) {
		this.inConnNum = inConnNum;
	}

	public long getOutConnNum() {
		return outConnNum;
	}

	public void setOutConnNum(long outConnNum) {
		this.outConnNum = outConnNum;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public List<Map<String, Object>> getClients() {
		return clients;
	}

	public void setClients(List<Map<String, Object>> clients) {
		this.clients = clients;
	}

	public List<Map<String, Object>> getResponses() {
		return responses;
	}

	public void setResponses(List<Map<String, Object>> responses) {
		this.responses = responses;
	}
}
