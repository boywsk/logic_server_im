package cn.com.gome.logic.pbTools;


import cn.com.gome.logic.model.MsgLocation;
import cn.com.gome.logic.protobuf.ProtoIM.ImMsgLocation;
import cn.com.gome.logic.protobuf.ProtoIM.KickUser;

import com.google.common.base.Strings;

/**
 * 消息对象和pb对象互转工具
 */
public class PbMsgTools {
	
	/**
	 * 位置对象转pb对象
	 * @param shop
	 * @return
	 */
	public static ImMsgLocation location2PbLocation(MsgLocation location) {
		ImMsgLocation.Builder pbLocation = ImMsgLocation.newBuilder();
		if(location == null) {
			return pbLocation.build();
		}
		String msgId = location.getMsgId();
		pbLocation.setMsgId(msgId);
		double longitude = location.getLongitude();
		pbLocation.setLongitude(longitude);
		
		double latitude = location.getLatitude();
		pbLocation.setLatitude(latitude);
		
		String imgUrl = location.getImgUrl();
		if(!Strings.isNullOrEmpty(imgUrl)) {
			pbLocation.setImgUrl(imgUrl);
		}
		
		String content = location.getContent();
		if(!Strings.isNullOrEmpty(content)) {
			pbLocation.setContent(content);
		}

		String extra = location.getExtra();
		if(!Strings.isNullOrEmpty(extra)) {
			pbLocation.setExtra(extra);
		}
		
		return pbLocation.build();
	}
	
	/**
	 * 位置pb对象转对象
	 * @param pbShop
	 * @return
	 */
	public static MsgLocation pbLocation2Location(ImMsgLocation pbLocation) {
		MsgLocation location = new MsgLocation();
		if(pbLocation == null) {
			return location;
		}
		String msgId = pbLocation.getMsgId();
		location.setMsgId(msgId);
		double longitude = pbLocation.getLongitude();
		location.setLongitude(longitude);
		double latitude = pbLocation.getLatitude();
		location.setLatitude(latitude);
		String imgUrl = pbLocation.getImgUrl();
		location.setImgUrl(imgUrl);
		String content = pbLocation.getContent();
		location.setContent(content);
		if(pbLocation.hasExtra()) {
			String extra = pbLocation.getExtra();
			location.setExtra(extra);
		}
		
		return location;
	}
	
	/**
	 * 生成踢出用户pb包
	 * @param uid
	 * @param deviceId
	 * @return
	 */
	public static KickUser pbKickUser(String appId, long uid, String token, String extra) {
		KickUser.Builder pbKickUser = KickUser.newBuilder();
		pbKickUser.setAppId(appId);
		pbKickUser.setUid(uid);
		pbKickUser.setToken(token);
		pbKickUser.setExtra(extra);
		
		return pbKickUser.build();
	}
}
