package cn.com.gome.logic.test.dao;

import java.util.ArrayList;
import java.util.List;

import cn.com.gome.logic.dao.SystemGroupMemberDao;
import cn.com.gome.logic.model.GroupMember;

public class SystemGroupDaoTest {

	public static void main(String[] args) {
		SystemGroupMemberDao dao = new SystemGroupMemberDao();
//		dao.updateMemberReadSeq("TEST_APP_ID", "10000", 100, 5);
		List<String> groupIds = new ArrayList<String>();
		groupIds.add("10000");
		groupIds.add("10001");
		List<GroupMember> list = dao.getMemberByGroupIdAndUid("TEST_APP_ID", groupIds, 100);
		System.out.println(list.size());
	}

}
