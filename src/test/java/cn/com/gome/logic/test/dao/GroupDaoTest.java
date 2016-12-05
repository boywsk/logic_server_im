package cn.com.gome.logic.test.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.dao.GroupDao;
import cn.com.gome.logic.dao.GroupMsgDao;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.model.Group;
import cn.com.gome.logic.model.GroupMsg;
import cn.com.gome.logic.utils.StringUtils;

public class GroupDaoTest {
	private static final Logger log = LoggerFactory.getLogger(GroupDaoTest.class);
	private final static String collName = "t_group_msg_";
	protected final static String dbMsgName = Global.DB_MSG_NAME;
	
	public static void main(String[] args) {
//		GroupDao dao = new GroupDao();
//		Group group = new Group();
//		group.setGroupId("2ad4c601de8e4c32b45b668d7d0b6ad0");
//		group.setType(2);
//		group.setSeq(0);
//		group.setCreateTime(System.currentTimeMillis());
//		group.setUpdateTime(System.currentTimeMillis());
////		System.out.println(group.getClass().getName());
////		dao.save("TEST_APP_ID", group);
//		
//		GroupMsg msg = new GroupMsg();
//		msg.setGroupId("123");
//		msg.setGroupType(1);
//		
//		dao.incGroupSeq("TEST_APP_ID", msg);
		
//		GroupMsgDao dao = new GroupMsgDao();
//		dao.getCollection("db_msg_gomeplus_test_4", "t_group_msg_0");
		
//		GroupDaoTest test = new GroupDaoTest();
//		String[] arr = test.getDBAndTableName("TEST_APP_ID", "d2f84163811f4e23897a548d7aa258b5");
//		System.out.println(arr[0]);
//		System.out.println(arr[1]);
		GroupDaoTest test = new GroupDaoTest();
		String[] arr = test.getDBAndTableName("gomeplus_pre", "0_658", 0);
		System.out.println(arr[0]);
		System.out.println(arr[1]);
	}
	
	/**
	 * 根据group计算库名和表名
	 * 
	 * @param groupId
	 * @return
	 */
	public String[] getDBAndTableName(String appId, String groupId) {
		log.info("[getDBAndTableName] appId=[{}],groupId=[{}]", appId, groupId);
		String[] arr = new String[2];
		int hashValue = StringUtils.FNVHash1(groupId);
		arr[0] = dbMsgName + "_" + appId.trim() + "_" + hashValue % Global.MSG_DB_MODULO;
		arr[1] = collName + hashValue % Global.MSG_TABLE_MODULO;

		return arr;
	}
	
	public String[] getDBAndTableName(String appId, String groupId, int traceId) {
		log.info("[getDBAndTableName] traceId=[{}], appId=[{}],groupId=[{}]",traceId, appId, groupId);
		String[] arr = new String[2];
		int hashValue = StringUtils.FNVHash1(groupId);
		arr[0] = dbMsgName + "_" + appId.trim() + "_" + hashValue % 64;
		hashValue = StringUtils.SDBMHash(groupId);
		arr[1] = collName + hashValue % 2;

		return arr;
	}
}
