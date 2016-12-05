package cn.com.gome.logic.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.dao.GroupDao;
import cn.com.gome.logic.dao.GroupMemberDao;
import cn.com.gome.logic.dao.SystemGroupMemberDao;
import cn.com.gome.logic.model.Group;
import cn.com.gome.logic.model.GroupMember;
import cn.com.gome.logic.pbTools.PbGroupMsgTools;
import cn.com.gome.logic.protobuf.ProtoIM.ImGroup;
import cn.com.gome.logic.protobuf.ProtoIM.UserData;

/**
 * 群组相关业务操作
 */
public class GroupService {
	private static Logger log = LoggerFactory.getLogger(GroupService.class);

	/**
	 * 获取用户群组
	 * @param appId
	 * @param uid
	 * @param time
	 * @return
	 */
	public UserData listGroupByUid(String appId, long uid, long time) {
		log.info("[listGroupByUid] appId=[{}],uid=[{}],time=[{}]", appId, uid, time);
		long lastTime = time;
		UserData.Builder pbGroupList = UserData.newBuilder();
		GroupDao dao = new GroupDao();
		GroupMemberDao memberDao = new GroupMemberDao();
//		GroupQuitMemberDao quitMemberDao = new GroupQuitMemberDao();
		List<GroupMember> members = memberDao.listMemberByUid(appId, uid);
		int memberSize = members.size();
		log.info("[listGroupByUid] appId=[{}],uid=[{}],time=[{}],members size=[{}]", appId, uid, lastTime, memberSize);
		for (GroupMember member : members) {
			int isBlocked = member.getIsMsgBlocked();
			String groupId = member.getGroupId();
			log.info("[listGroupByUid] appId=[{}],uid=[{}],member groupId=[{}]", appId, uid, groupId);
			Group group = dao.getGroupById(appId, groupId);
			if(group == null) {
				continue;
			}
			int isDel = group.getIsDele();
			long updateTime = group.getUpdateTime();
			long readSeq = member.getReadSeq();
			long seq = group.getSeq();
			if(seq == 0 || isDel == 1) {
				continue;
			}
			if(isBlocked == 0) {
				if(seq <= readSeq || updateTime < lastTime) {
					continue;
				}
			} else {
				if(lastTime > 0 && updateTime < lastTime) {
					continue;
				}
			}
			log.info("[listGroupByUid] groupId=[{}], uid=[{}], seq=[{}], intSeq=[{}],readSeq=[{}],isBlocked=[{}]", groupId, member.getUid(),
					group.getSeq(), member.getInitSeq(), member.getReadSeq(), isBlocked);
			ImGroup pbGroup = PbGroupMsgTools.grou2PbGroup(group, member);
			log.info("[====1================listGroupByUid] groupId=[{}], uid=[{}], seq=[{}], intSeq=[{}],readSeq=[{}],isBlocked=[{}]", groupId, member.getUid(),
					pbGroup.getSeqId(), pbGroup.getInitSeqId(), pbGroup.getReadSeqId(), pbGroup.getIsMsgBlocked());
			pbGroupList.addGroup(pbGroup);
		}
//		if (time >= 0) {
//			List<GroupQuitMember> quitMembers = quitMemberDao.listGroupQuitMember(appId, uid, lastTime);
//			if(quitMembers != null) {
//				for (GroupQuitMember member : quitMembers) {
//					String groupId = member.getGroupId();
//					log.info("[====2================listGroupByUid] groupId=[{}], uid=[{}]", groupId, member.getUid());
//					Group group = dao.getGroupById(appId, groupId);
//					int groupType = 0;
//					if(group != null) {
//						groupType = group.getType();
//					}
//					ImGroup pbGroup = PbGroupMsgTools.grou2PbGroup(groupType, member.getGroupId());
//					if (pbGroup == null) {
//						continue;
//					}
//					log.info("[====3================listGroupByUid] isQuit=[{}], uid=[{}]", pbGroup.getIsQuit(), member.getUid());
//					pbGroupList.addGroup(pbGroup);
//				}
//			}
//			
//		}

		return pbGroupList.build();
	}
	
	/**
	 * 获取用户群组
	 * @param appId
	 * @param uid
	 * @param time
	 * @return
	 */
	public UserData listSystemGroupByUid(String appId, long uid, long time) {
		log.info("[listSystemGroupByUid] appId=[{}],uid=[{}],time=[{}]", appId, uid, time);
		long lastTime = time;
		UserData.Builder pbGroupList = UserData.newBuilder();
		GroupDao dao = new GroupDao();
		SystemGroupMemberDao systemGroupMemberDao = new SystemGroupMemberDao();
		//先注释掉========================================
//		if(time == 0) {
//			UserDao userDao = new UserDao();
//			User user = userDao.getUser(appId, uid);
//			lastTime = user.getUpdateTime();
//		}
		List<Group> list = dao.listSystemGroup(appId, lastTime);
		List<String> groupIds = new ArrayList<String>();
		
		for(Group group : list) {
			String groupId = group.getGroupId();
			groupIds.add(groupId);
		}
		Map<String, GroupMember> map = new HashMap<String, GroupMember>();
		List<GroupMember> members = systemGroupMemberDao.getMemberByGroupIdAndUid(appId, groupIds, uid);
		for(GroupMember member : members) {
			map.put(member.getGroupId(), member);
		}
		for(Group group : list) {
			String groupId = group.getGroupId();
			GroupMember member = map.get(groupId);
			//先注释掉========================================
			long seq = group.getSeq();
			if(member != null) {
				long readSeq = member.getReadSeq();
				if(readSeq >= seq) {
					continue;
				}
			}
			
			ImGroup pbGroup = PbGroupMsgTools.grou2PbGroup(group, member);
			if (pbGroup != null) {
				pbGroupList.addGroup(pbGroup);
			}
		}
		
		return pbGroupList.build();
	}
	
	/**
	 * 根据群组id获取群组消息
	 * @param uid
	 * @param groupId
	 * @return
	 */
	public UserData getGrpupById(String appId, long uid, String groupId) {
		log.info("appId=[{}],uid=[{}],groupId=[{}]",appId, uid, groupId);
		UserData.Builder pbGroupList = UserData.newBuilder();
		GroupDao dao = new GroupDao();
		GroupMemberDao memberDao = new GroupMemberDao();
		Group group = dao.getGroupById(appId, groupId);
		GroupMember member = memberDao.getMemberByGroupIdAndUid(appId, groupId, uid);
		ImGroup pbGroup = PbGroupMsgTools.grou2PbGroup(group, member);
		if(pbGroup != null) {
			log.info("appId=[{}],uid=[{}],groupId=[{}]",appId, uid, groupId);
			pbGroupList.addGroup(pbGroup);
		}
		
		return pbGroupList.build();
	}
}
