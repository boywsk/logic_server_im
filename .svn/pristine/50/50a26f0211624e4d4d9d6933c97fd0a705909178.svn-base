package cn.com.gome.logic.apns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

import cn.com.gome.logic.dao.UserDao;
import cn.com.gome.logic.dao.UserRedisDao;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.model.GroupMsg;
import cn.com.gome.logic.model.User;
import cn.com.gome.logic.protobuf.ProtoIM.ImMsg;
import cn.com.gome.logic.protobuf.ProtocolPackage;
import cn.com.gome.logic.utils.ProtocolPackageUtils;
import cn.com.gome.logic.utils.StringUtils;

/**
 * apns消息推送 https://github.com/notnoop/java-apns
 */
public class ApnsPushManager {
	static Logger log = LoggerFactory.getLogger(ApnsPushManager.class);
	private final static String keystorePath = StringUtils.getRealPath() + "/config/" + Global.APNS_EKYSTORE_PATH;
	private final static String keystorePassword = Global.APNS_EKYSTORE_PASSWORD;
	static ApnsService service = null;

	static {
		log.info("[static] keystorePath:[{}], keystorePassword:[{}]", keystorePath, keystorePassword);
		service = APNS.newService().withCert(keystorePath, keystorePassword).withSandboxDestination()
				// .withProductionDestination()
				.build();
	}

	/**
	 * 消息推送
	 * 
	 * @param data
	 */
	public void push(byte[] data) {
		UserRedisDao redisDao = new UserRedisDao();
		UserDao dao = new UserDao();
		try {
			ProtocolPackage pack = ProtocolPackageUtils.byteArray2Package(data);
			long receiverId = pack.getReceiveId();
			String appId = pack.getAppId().trim();
			ImMsg msg = ImMsg.parseFrom(pack.getProtoBody());
			String title = getTitle(msg);
			User user = dao.getIOSUser(appId, receiverId);
			if(user == null) {
				log.error("[push] user is null or not ios!!!");

				return;
			}
			String deviceToken = user.getApnsToken();
			if (deviceToken == null || deviceToken.length() <= 0) {
				log.error("[push] token is null or empty!!!");

				return;
			}
			long count = redisDao.incPushCount(appId, receiverId, deviceToken);
			String payload = APNS.newPayload().badge((int) count).alertBody(title).sound("default").build();
			// 发送
			service.push(deviceToken, payload);
			log.info("[push] appId=[{}],deviceToken=[{}],badge=[{}]", appId, deviceToken, count);
		} catch (Exception e) {
			log.error("push:", e);
		}
	}

	/**
	 * 消息推送
	 * 
	 * @param token
	 * @param receiverId
	 * @param msg
	 */
	public void push(String appId, String token, long receiverId, GroupMsg msg) {
		log.info("[push] appId=[{}],receiverId=[{}],deviceToken=[{}]", appId, receiverId, token);
		String title = getTitle(msg);
		UserRedisDao redisDao = new UserRedisDao();
		long count = redisDao.incPushCount(appId, receiverId, token);
		String payload = APNS.newPayload().badge((int) count).alertBody(title).sound("default").build();
		// 发送
		service.push(token, payload);
		log.info("[push] receiverId=[{}],deviceToken=[{}],badge=[{}]", receiverId, token, count);
	}

	/**
	 * 提示信息
	 * 
	 * @param msg
	 * @return
	 */
	private String getTitle(ImMsg msg) {
//		String sendName = msg.getSenderName();
//		int msgType = msg.getMsgType();
//		if (msgType == Constant.TEXT.value) {
//			String body = msg.getMsgBody();
//			if (body != null && body.length() >= 50) {
//				body = body.substring(0, 50) + "......";
//			}
//			String title = sendName + body;
//
//			return title;
//		}
//		String title = sendName + Constant.PUSH_CONTENT(msgType);

//		return sendName + title;
		return "你有一条新消息";
	}

	/**
	 * 提示信息
	 * 
	 * @param msg
	 * @return
	 */
	private String getTitle(GroupMsg msg) {
//		String sendName = msg.getSenderName();
//		int msgType = msg.getMsgType();
//		if (msgType == Constant.MESSAGE_TYPE.E_MESSAG_TYPE_TEXT.value) {
//			String body = msg.getMsgBody();
//			if (body != null && body.length() >= 50) {
//				body = body.substring(0, 50) + "......";
//			}
//			String title = sendName + body;
//
//			return title;
//		}
//		String title = sendName + Constant.PUSH_CONTENT(msgType);
//
//		return sendName + title;
		return "你有一条新消息";
	}

	public static void main(String[] args) throws Exception {
		// int id = EnhancedApnsNotification.INCREMENT_ID();
		// id = EnhancedApnsNotification.INCREMENT_ID();
		// id = EnhancedApnsNotification.INCREMENT_ID();

		// EnhancedApnsNotification notification = new
		// EnhancedApnsNotification(EnhancedApnsNotification.INCREMENT_ID() /*
		// Next ID */,
		// now + 60 * 60 /* Expire in one hour */,
		// token /* Device Token */,
		// payload);

		// System.out.println(id);
		String alter = "你有一条新消息";
//		alter = alter.substring(0, 50) + "......";
		String payload = APNS.newPayload().badge(13)
				.alertBody(alter)
				.sound("default").build();
		System.out.println(payload.getBytes("UTF-8").length);
		service.push("a64292404c3b063865ba5da98de6e505ceb907e76282e6e7e579a54710a6480d", payload);
	}
}
