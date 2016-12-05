package cn.com.gome.logic.test.json;

import com.alibaba.fastjson.JSON;

import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.model.GroupMsg;
import cn.com.gome.logic.model.MQMsg;
import cn.com.gome.logic.utils.StringUtils;

public class Test {

	public static void main(String[] args) {
			int hashValue = StringUtils.FNVHash1("groupId");
			System.out.println(hashValue % Global.MSG_DB_MODULO);
			System.out.println(hashValue % Global.MSG_TABLE_MODULO);

		
//		MQMsg msg = new MQMsg();
////		GroupMsg msg = new GroupMsg();
//		msg.setAppId("appid");
//		msg.setCmd((short)1);
//		//msg.setPersist(true);
//		String str = JSON.toJSONString(msg);
//		System.out.println(str);
//		
//		GroupMsg m = JSON.parseObject(str, GroupMsg.class);
//		System.out.println(m.getGroupId());
	}

}
