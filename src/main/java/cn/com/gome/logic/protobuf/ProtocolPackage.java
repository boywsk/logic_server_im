package cn.com.gome.logic.protobuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class ProtocolPackage implements Serializable /* Cloneable */ {
	private static final long serialVersionUID = 5789982589908808353L;
	private static Logger log = LoggerFactory.getLogger(ProtocolPackage.class);

	private byte[] head;
	private byte startTag; // 包开始标示 byte
	private short length; // 包头+包体总长
	private short command; // 命令字
	private long uid; // userId
	private byte clientId; // ios/android/pc/mac....
	private byte iVersion; // 协议版本号
	private byte clientType; // 客户端类型;0:IM/1:push
	private byte result; // response时的error code
	private long stime; // 服务端时间
	private long receiveId;// 消息接受者
	private long rspIP; // gateWay ip
	private int rspPort; // gateWay port
	private int traceId; // 染色id/跟踪id
	private byte ack; // 回包标记(是否是请求应答包)；0:否、1:是
	private String appId; // 应用id 32位
	private byte[] sResv; // 补位;byte[15]
	private byte[] protoBody; // protobuf内容 byte[]
	private byte endTag; // 包结束标示 byte

	public byte[] getHead() {
		return head;
	}

	public void setHead(byte[] head) {
		this.head = head;
	}

	public byte getStartTag() {
		return startTag;
	}

	public void setStartTag(byte startTag) {
		this.startTag = startTag;
	}

	public short getLength() {
		return length;
	}

	public void setLength(short length) {
		this.length = length;
	}

	public short getCommand() {
		return command;
	}

	public void setCommand(short command) {
		this.command = command;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public byte getClientId() {
		return clientId;
	}

	public void setClientId(byte clientId) {
		this.clientId = clientId;
	}

	public byte getiVersion() {
		return iVersion;
	}

	public void setiVersion(byte iVersion) {
		this.iVersion = iVersion;
	}

	public byte getClientType() {
		return clientType;
	}

	public void setClientType(byte clientType) {
		this.clientType = clientType;
	}

	public byte getResult() {
		return result;
	}

	public void setResult(byte result) {
		this.result = result;
	}

	public long getStime() {
		return stime;
	}

	public void setStime(long stime) {
		this.stime = stime;
	}

	public long getReceiveId() {
		return receiveId;
	}

	public void setReceiveId(long receiveId) {
		this.receiveId = receiveId;
	}

	public long getRspIP() {
		return rspIP;
	}

	public void setRspIP(long rspIP) {
		this.rspIP = rspIP;
	}

	public int getRspPort() {
		return rspPort;
	}

	public void setRspPort(int rspPort) {
		this.rspPort = rspPort;
	}

	public byte getAck() {
		return ack;
	}

	public void setAck(byte ack) {
		this.ack = ack;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public int getTraceId() {
		return traceId;
	}

	public void setTraceId(int traceId) {
		this.traceId = traceId;
	}

	public byte[] getsResv() {
		return sResv;
	}

	public void setsResv(byte[] sResv) {
		this.sResv = sResv;
	}

	public byte[] getProtoBody() {
		return protoBody;
	}

	public void setProtoBody(byte[] protoBody) {
		this.protoBody = protoBody;
	}

	public byte getEndTag() {
		return endTag;
	}

	public void setEndTag(byte endTag) {
		this.endTag = endTag;
	}

	public String toString() {
		return JSON.toJSONString(this);
	}
	//
	// public ProtocolPackage clone() {
	// try {
	// return (ProtocolPackage) super.clone();
	// } catch (CloneNotSupportedException e) {
	// log.error("[clone]:", e);
	// return null;
	// }
	// }

	public ProtocolPackage deepCopy() {
		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;
		ByteArrayInputStream bis = null;
		ObjectInputStream ois = null;
		try {
			// 将该对象序列化成流,因为写在流里的是对象的一个拷贝，而原对象仍然存在于JVM里面。所以利用这个特性可以实现对象的深拷贝
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(this);
			// 将流序列化成对象
			bis = new ByteArrayInputStream(bos.toByteArray());
			ois = new ObjectInputStream(bis);

			return (ProtocolPackage) ois.readObject();
		} catch (Exception e) {
			log.error("[deepCopy]:", e);
			return null;
		} finally {
			try {
				if (bos != null) {
					bos.close();
					bos = null;
				}
				if (oos != null) {
					oos.close();
					oos = null;
				}
				if (bis != null) {
					bis.close();
					bis = null;
				}
				if (ois != null) {
					ois.close();
					ois = null;
				}
			} catch (IOException e) {
				log.error("[deepCopy close]:", e);
			}
		}
	}
}
