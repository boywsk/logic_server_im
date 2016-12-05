package cn.com.gome.logic.global;

import java.util.HashMap;
import java.util.Map;

/**
 * 常量、枚举等定义
 */
public class Constant {
	
	public static final String REDIS_GROUP_MEMBER_SUFFIX = "_members";
	
	// 踢/退出群
	public static enum QUIT_TYPE {
		PASSIVE(1), // 被踢/被动退群
		INITIATIVE(2); // 主动退群

		public int value;
		private QUIT_TYPE(int value) {
			this.value = value;
		}
	}
	// 功能性消息
	public static enum NOTICEMSG_TYPE {
		QUITGROUPMSG(1),
		ISSUEREVOKEMSG(2);
		/*ADDFRIENDMSG(100), // 申请添加好友
		DELFRIENDMSG(101), // 删除好友
		AGREEFRIENDMSG(102), // 同意/拒绝好友申请
		APPLYJOINGROUPMSG(200), // 申请加入群
		NOTICEMANAGERMSG(201), // 通知管理员审核加入成员
		INVITEDJOINGROUPMSG(202), // 邀请加入群
		NOTICEAPPLICANTMSG(203), // 通知被邀请加入群
		QUITGROUPMSG(204), // 退/踢出群
		EDITGROUPMSG(205), // 修改群信息
		DISBANDGROUPMSG(206), // 解散群
		CHANGEGROUPMANAGERMSG(207), // 变更群主通知
		USERMODIFYMSG(208), // 用户修改通知
		ISSUEREVOKEMSG(209),// 撤消消息通知
		SCANQRJOINGROUPMSG(210);//扫码加入群
*/
		public int value;
		private NOTICEMSG_TYPE(int value) {
			this.value = value;
		}
	}

	// 终端设备类型
	public static enum DEVICE_TYPE {
		IOS((byte) 10), // ios设备
		ANDROID((byte) 11), // android设备
		WP((byte) 12), // windows phone设备
		WIN((byte) 20), // windows pc
		MAC((byte) 21), // 苹果mac pc
		UBUNTU((byte) 22), // ubuntu
		LINUX((byte) 23), // linux
		UNIX((byte) 24), // unix
		IPAD((byte) 25), // ipad
		WEB((byte) 30), // web
		H5((byte) 40); // h5
		
		public byte value;

		private DEVICE_TYPE(byte value) {
			this.value = value;
		}
	}

	// 群组类型
	public static enum CHAT_TYPE {
		SINGLE(1), // 单聊
		GROUP(2), // 群聊
		SYS(3), // 系统信息
		HELPER(4), // 小秘书
		CUSTOMER(5); // 客服

		public int value;

		private CHAT_TYPE(int value) {
			this.value = value;
		}
	}

	// 消息类型
	public enum MESSAGE_TYPE {
		TEXT(1), // 文本
		VOICE(2), // 语音
		IMG(3), // 图片
		VIDEO(4), // 视频
		POSITION(5), // 位置
		ATTACH(6), // 附件
		GROUP_OPT(7), // 群组操作通知
		PASSTHROUGH(99); // 消息透传，在扩展字段中

		public int value;

		MESSAGE_TYPE(int value) {
			this.value = value;
		}
	}

	// 服务器端返回包类型
	public enum PACK_ACK {
		NO((byte) 0), // 请求应打包
		YES((byte) 1); // 转发包

		public byte value;

		PACK_ACK(byte value) {
			this.value = value;
		}
	}
	
	// 报文超长时分包，分包是否是最后一个包
	public enum LAST_PACK {
		NO((byte) 0), // 否
		YES((byte) 1); // 是

		public byte value;
		LAST_PACK(byte value) {
			this.value = value;
		}
	}

	// result返回状态吗
	public enum REULT_CODE {
		OK((byte) 0), // 成功
		ERR((byte) -1), // 错误，细分再详细定义
		TOKEN_ERR((byte) -2), // token错误
		USER_NOT_EXIT((byte) -3), // 用户不存在
		TOKEN_TIMEOUT((byte) -4), // token过期
		NOT_IN_GROUP((byte) -5), // 用户不在该群组
		REVOEK_MSG_OUTTIME((byte) -6); // 撤消消息超时

		public byte value;

		REULT_CODE(byte value) {
			this.value = value;
		}
	}
	
	//请求类型
		public static enum REQUEST_TYPE {
			REPORT(1),          // 汇报服务资源
			GET_RESOURCES(2);   // 获取服务资源

			public int value;
			private REQUEST_TYPE(int value) {
				this.value = value;
			}
		}
		
		//服务类型：
		public static enum SERVER_TYPE {
			GATEWAY(1), // 接入
			LOGIC(2),   // 逻辑
			API(3),     // api
			FILE(4),    // 文件
			ALL(5);     //全部

			public int value;
			private SERVER_TYPE(int value) {
				this.value = value;
			}
		}

	// 消息推送内容
	public static String PUSH_CONTENT(int type) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("" + Constant.MESSAGE_TYPE.VOICE.value, "发来一段语音");
		map.put("" + Constant.MESSAGE_TYPE.IMG.value, "发来一张图片");
		map.put("" + Constant.MESSAGE_TYPE.VIDEO.value, "发来一段视频");
		map.put("" + Constant.MESSAGE_TYPE.POSITION.value, "发来坐标位置");
		map.put("" + Constant.MESSAGE_TYPE.ATTACH.value, "发来文件");

		return map.get("" + type);
	}
}
