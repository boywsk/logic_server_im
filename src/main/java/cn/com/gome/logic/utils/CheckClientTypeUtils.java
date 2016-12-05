package cn.com.gome.logic.utils;

public class CheckClientTypeUtils {

	/**
	 * 客户端类型--mobile--移动端
	 * 
	 * @param 10:ios/11:android/12:wp
	 * @param clientId
	 */
	public static boolean clientType_mobile(int clientId) {
		int[] mobileList = { 10, 11, 12 };
		for (int s : mobileList) {
			if (s == clientId)
				return true;
		}
		return false;
	}

	/**
	 * 客户端类型--PC端
	 * 
	 * @param 20:pc/21:mac/22:ubuntu/23:linux/24:unix
	 * @param clientId
	 */
	public static boolean clientType_pc(int clientId) {
		int[] PCList = { 20, 21, 22, 23, 24 };
		for (int s : PCList) {
			if (s == clientId)
				return true;
		}
		return false;
	}

}
