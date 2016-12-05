package cn.com.gome.logic.dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

import cn.com.gome.logic.model.GroupMember;
import cn.com.gome.logic.utils.BeanTransUtils;

public class SystemGroupMemberDao extends BaseDao {
	private static final Logger log = LoggerFactory.getLogger(SystemGroupMemberDao.class);
	private final static String collName = "t_system_group_member";
	
	/**
	 * 修改群组成员消息读取seq
	 * @param appId
	 * @param groupId
	 * @param uid
	 * @param readSeq
	 */
	public void updateMemberReadSeq(String appId, String groupId, long uid, long readSeq) {
		log.info("[updateMemberReadSeq] appId=[{}],groupId=[{}],uid=[{}],readSeq=[{}]", appId, groupId, uid, readSeq);
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
		Bson filter = Filters.and(Filters.eq("groupId", groupId), Filters.eq("uid", uid));
		Document upDoc = new Document();
		upDoc.append("readSeq", readSeq);
		upDoc.append("updateTime", System.currentTimeMillis());
		Document update = new Document("$set", upDoc);
//		FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
//		options.upsert(true);
//		Document doc = coll.findOneAndUpdate(filter, update, options);
//		if(doc == null) {
//			System.out.println("==============");
//		}
		UpdateOptions options = new UpdateOptions();
		options.upsert(true);
		coll.updateOne(filter, update, options);
	}
	
	/**
	 * 根据群组id和用户id获取群组用户信息
	 * @param appId
	 * @param groupIds
	 * @param uid
	 * @return
	 */
	public List<GroupMember> getMemberByGroupIdAndUid(String appId, List<String> groupIds, long uid) {
		log.info("[getMemberByGroupIdAndUid] appId=[{}],groupIds seize=[{}],uid=[{}]", appId, groupIds.size(), uid);
		List<GroupMember> list = new ArrayList<GroupMember>();
		if(groupIds != null && groupIds.size() > 0) {
			MongoCollection<Document> coll = this.getAppCollection(appId, collName);
			Bson where = Filters.and(Filters.in("groupId", groupIds), Filters.eq("uid", uid));
			MongoCursor<Document> docs = coll.find(where).iterator();
			while (docs.hasNext()) {
				Document doc = docs.next();
				GroupMember member = (GroupMember) BeanTransUtils.document2Bean(doc, GroupMember.class);
				log.info("[getMemberByGroupIdAndUid] appId=[{}],groupId=[{}],uid=[{}]", appId, member.getGroupId(), uid);
				list.add(member);
			}
		}
		
		return list;
	}
}
