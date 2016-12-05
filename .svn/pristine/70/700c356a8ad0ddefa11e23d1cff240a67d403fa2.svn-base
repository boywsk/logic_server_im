package cn.com.gome.logic.service;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.utils.TcpDataSendUtils;
import cn.com.gome.logic.dao.UserDao;
import cn.com.gome.logic.dao.UserRedisDao;
import cn.com.gome.logic.global.Command;
import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.global.Constant.DEVICE_TYPE;
import cn.com.gome.logic.global.Constant.REULT_CODE;
import cn.com.gome.logic.model.User;
import cn.com.gome.logic.pbTools.PbMsgTools;
import cn.com.gome.logic.protobuf.ProtoIM.CleanPushCount;
import cn.com.gome.logic.protobuf.ProtoIM.KickUser;
import cn.com.gome.logic.protobuf.ProtoIM.UserLogin;
import cn.com.gome.logic.protobuf.ProtoIM.UserLogout;
import cn.com.gome.logic.protobuf.ProtocolPackage;

/**
 * 用户相关业务操作
 */
public class UserService {
	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	/**
	 * 
	 * 用户通过token登录 1、判断用户的合法性 2、判断终端登录情况，一个类型(移动/pc)的终端同事只能登录一个
	 * @param clientKey
	 * @param msg
	 * @return
	 */
	public int userLogin(String clientKey, ProtocolPackage msg) {
		try {
			byte[] body = msg.getProtoBody();
			UserLogin pbLogin = UserLogin.parseFrom(body);
			String appId = msg.getAppId().trim();
			long uid = pbLogin.getUid();
			String token = pbLogin.getToken();
			String apnsToken = pbLogin.getApnsToken().trim();
			String deviceId = pbLogin.getDeviceId();
			byte clientId = msg.getClientId();
			byte clientType = msg.getClientType();
			long rspIP = msg.getRspIP();
			int rspPort = msg.getRspPort();
			long currTime = System.currentTimeMillis();
			
			log.info("{}|{}|{}|{}|{}", "σσlogin", uid, clientId, appId, currTime);
			
			log.info("[userLogin] traceId=[{}],appId=[{}],cmd=[{}],uid=[{}],clientId=[{}],token=[{}],apnsToken=[{}],deviceId=[{}]",
					msg.getTraceId(), appId, msg.getCommand(), uid, clientId, token, apnsToken, deviceId);
			if(StringUtils.isEmpty(token)) {//token为空
				log.info("[userLogin] token is empty!!!");
				return Constant.REULT_CODE.TOKEN_ERR.value;
			}
			token = token.trim();
			UserDao userDao = new UserDao();
			User user = userDao.getUser(appId, uid);
			if(null == user) {
				log.error("[userLogin] user not exit!!! traceId=[{}],uid=[{}]", msg.getTraceId(), uid);
				return REULT_CODE.USER_NOT_EXIT.value;
			}
			String dbToken = "";
			long tokenDeadline = 0;
			//根据不同的客户端，获取不同的Token
			if(clientId == DEVICE_TYPE.WEB.value){ //web端
				dbToken = user.getWebToken();
				tokenDeadline = user.getWebTokenExpires();
			}else{
				dbToken = user.getToken();
				tokenDeadline = user.getTokenExpires();
			}
			 
//			String dbApnstoken = user.getApnsToken();
			String dbDeviceId = user.getDeviceId();
			if(dbDeviceId == null) {
				dbDeviceId = "";
			}
//			if (clientId >= 20) {// 非移动端
//				token = user.getPcToken();
//				tokenDeadline = user.getPcTokenDeadline();
//			}
			log.info("[userLogin] traceId=[{}],dbToken=[{}],tokenExpires=[{}],dbDeviceId=[{}]", msg.getTraceId(), dbToken, tokenDeadline, dbDeviceId);
			if(!dbToken.equals(token)) {//token错误
				log.error("[userLogin] token is error!!! traceId=[{}],uid=[{}]", msg.getTraceId(), uid);
				return Constant.REULT_CODE.TOKEN_ERR.value;
			}
			if(tokenDeadline < currTime) {//token过期
				log.error("[userLogin] token timeout!!! traceId=[{}],uid=[{}]", msg.getTraceId(), uid);
				return Constant.REULT_CODE.TOKEN_TIMEOUT.value;
			}
			
			UserRedisDao userRedisDao = new UserRedisDao();
			Map<String, String> map = userRedisDao.listUserRsp(appId, uid);
			String startFlag = String.valueOf(clientId).substring(0, 1);
			if (map != null) {
				for (String key : map.keySet()) {
					String[] keyArr = key.split("_");
					if (keyArr.length < 2) {
						continue;
					}
					String oldClientId = keyArr[0];
					byte bOldClientId = Integer.valueOf(oldClientId).byteValue();
					String oldDeviceId = keyArr[1];
					if (oldClientId.startsWith(startFlag) && !oldDeviceId.equals(deviceId)) {// 有相同类型的终端踢下;并且不是本机
						ProtocolPackage pack = new ProtocolPackage();
						pack.setHead(msg.getHead());
						pack.setCommand(Command.CMD_USER_KICK);
						String oldValue = map.get(key);
						String[] valueArr = oldValue.split(":");
						if (valueArr.length >= 2) {
							long rspIp = Long.valueOf(valueArr[0]);
							int rspport = Integer.parseInt(valueArr[1]);
							if (rspport > 0) {
								pack.setRspIP(rspIp);
								pack.setRspPort(rspport);
								pack.setClientId(bOldClientId);
								pack.setReceiveId(msg.getUid());
								KickUser kickUser = PbMsgTools.pbKickUser(appId, uid, oldDeviceId, "");
								pack.setProtoBody(kickUser.toByteArray());
								// 发送踢下另一端的消息
								log.info("kick another;traceId=[{}],appId=[{}],uid=[{}],oldDeviceId=[{}] to offline",
										msg.getTraceId(), appId, uid, kickUser.getToken());
								TcpDataSendUtils.sendTcpData(clientKey, pack);
							}
							// 删除redis记录
							userRedisDao.delUserRsp(appId, uid, bOldClientId, oldDeviceId);
						}
					}
				}
			}
			if (rspPort > 0) {
				String value = rspIP + ":" + rspPort;
				userRedisDao.saveUserRsp(appId, uid, clientId, deviceId, value);
			}
			// ios
			if (clientId == Constant.DEVICE_TYPE.IOS.value) {
				// 清除计数
				userRedisDao.cleanPushCount(appId, uid, apnsToken);
			}
			// 修改用户设备类型和设备标识
			userDao.updateUser(appId, uid, clientId, clientType, apnsToken, deviceId);
		} catch (Exception e) {
			log.error("[userLogin] cause is:", e);
			return -1;
		}

		return 0;
	}

	/**
	 * 用户退出；清除和用户在线相关的信息
	 * 
	 * @param clientKey
	 * @param msg
	 */
	public void userLogout(String clientKey, ProtocolPackage msg) {
		UserRedisDao userRedisDao = new UserRedisDao();
		try {
			byte[] body = msg.getProtoBody();
			UserLogout pbLogout = UserLogout.parseFrom(body);
			long uid = pbLogout.getUid();
			String token = pbLogout.getToken().trim();
			String appId = msg.getAppId().trim();
			byte clientId = msg.getClientId();
			log.info("[userLogout] appId=[{}],uid=[{}],clientId=[{}],token=[{}],cmd=[{}]", appId, uid, clientId, token,
					msg.getCommand());
			// 删除redis记录
			userRedisDao.delUserRsp(appId, uid, clientId, token);
			// ios
			if (clientId == (byte) Constant.DEVICE_TYPE.IOS.value) {
				// 删除token
				UserDao dao = new UserDao();
				dao.updateApnsToken(appId, uid, "");
			}
		} catch (Exception e) {
			log.error("[userLogout] cause is:", e);
		}
	}

	/**
	 * 清除消息推送计数
	 * 
	 * @param clientKey
	 * @param msg
	 */
	public void cleanPushCount(String clientKey, ProtocolPackage msg) {
		try {
			UserRedisDao userRedisDao = new UserRedisDao();
			byte[] body = msg.getProtoBody();
			CleanPushCount cleanCount = CleanPushCount.parseFrom(body);
			long uid = cleanCount.getUid();
			String apnsToken = cleanCount.getApnsToken();
			String appId = msg.getAppId().trim();
			byte clientId = msg.getClientId();

			log.info("[cleanPushCount] appId=[{}],uid=[{}],clientId=[{}],apnsToken=[{}]", appId, uid, clientId,
					apnsToken);
			userRedisDao.cleanPushCount(appId, uid, apnsToken);
		} catch (Exception e) {
			log.error("[cleanPushCount] cause is:", e);
		}

	}
}
