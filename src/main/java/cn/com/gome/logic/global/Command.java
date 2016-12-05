package cn.com.gome.logic.global;

/**
 * 命令字定义
 */
public class Command {
	// 心跳;1
	public final static short CMD_HEARTBEAT = 0x0001;
	// 用户登录;2
	public final static short CMD_USER_LOGIN = 0x0002;
	// 用户登出;3
	public final static short CMD_USER_LOGOUT = 0x0003;
	// 用户登出;4
	public final static short CMD_USER_KICK = 0x0004;
	// 强制关闭用户连接;5
	public final static short CMD_CLOSE_CHANNEL = 0x0005;
	// 客户端拉取获取聊天群组;256
	public final static short CMD_LIST_GROUP = 0x0100;
	// 客户端拉取获取系统信息(全站广播)群组;257
	public final static short CMD_LIST_SYS_GROUP = 0x0101;

	// 客户端发送IM消息，包括group消息和单聊消息;513
	public final static short CMD_IM_SEND_MSG = 0x0201;
	// 客户端拉取离线消息或增量消息同步;514
	public final static short CMD_IM_OFFLINE_MSG = 0x0202;
	// 上报/提交readSeqId;515
	public final static short CMD_SUBMIT_READ_SEQ = 0x0203;
	// 下发/转发readSeqId；转发给在线另一类型终端;516
	public final static short CMD_ISSUE_READ_SEQ = 0x0204;
	// 咨询类聊天信息(客服类等);517
	public final static short CMD_CONSULT_IM_MSG = 0x0205;
	// 全站广播;518
	public final static short CMD_BROADCAST_IM_MSG = 0x0206;

	// 上报/提交initSeqId;521
	public final static short CMD_SUBMIT_INIT_SEQ = 0x0209;
	// 下发/转发initSeqId；转发给在线另一类型终端;522
	public final static short CMD_ISSUE_INIT_SEQ = 0x0210;
	// 撤消消息;522
	public final static short CMD_REVOKE_MSG = 0x020A;
	// 功能性离线消息获取;524
	public final static short CMS_OFFLINE_NOTICE_MSG = 0x020C;
	// 功能性消息;525
	public final static short CMD_NOTICE_MSG = 0x020D;
	// 收到功能消息确认
	public final static short CMD_NOTICE_ACK = 0x020E;
	// 根据群组id获取群组消息；返回数据在UserData ImGroup列表中;523
	public final static short CMD_GROUP_BY_ID = 0x0211;

	// push消息;768
	public final static short CMD_PUSH_MSG = 0x0300;
	// 清除push计数;769
	public final static short CMD_CLEAN_PUSH_COUNT = 0x0301;
}
