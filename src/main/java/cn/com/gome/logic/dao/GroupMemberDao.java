package cn.com.gome.logic.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cn.com.gome.logic.model.GroupMember;
import cn.com.gome.logic.utils.BeanTransUtils;
import cn.com.gome.logic.utils.JedisUtils;
import redis.clients.jedis.JedisCluster;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

public class GroupMemberDao extends BaseDao {

	private static final Logger log = LoggerFactory.getLogger(GroupMemberDao.class);

	// private final static String dbName = "db_im";
	private final static String collName = "t_group_member";

//	/**
//	 * 保存群成员
//	 * 
//	 * @param member
//	 */
//	public void save(GroupMember member) {
//		log.info("groupId = [{}],uid = [{}]", member.getGroupId(), member.getUid());
//		Document doc = BeanTransUtils.bean2Document(member);
//		this.insert(dbName, collName, doc);
//	}

//	/**
//	 * 修改群成员备注
//	 * 
//	 * @param groupId
//	 * @param uid
//	 * @param markedUid
//	 * @param mark
//	 */
//	public void updateMemberMark(String groupId, long uid, long markedUid, String mark) {
//		log.info("start...,groupId = [{}],uid = [{}],markedUid is = [{}],mark = [{}]", groupId, uid, markedUid, mark);
//		MongoCollection<Document> coll = this.getCollection(dbName, collName);
//		Bson filter = Filters.and(Filters.eq("groupId", groupId), Filters.eq("uid", uid),
//				Filters.eq("membersMark.markedUid", markedUid));
//		MongoCursor<Document> cursor = this.find(dbName, collName, filter);
//		if (cursor.hasNext()) {
//			Document update = new Document("$set", new Document("membersMark.$.mark", mark));
//			coll.updateOne(filter, update);
//		} else {
//			GroupMemberMark memberMark = new GroupMemberMark();
//			memberMark.setMarkedUid(markedUid);
//			memberMark.setMark(mark);
//			Document markDoc = BeanTransUtils.bean2Document(memberMark);
//			Document update = new Document("$addToSet", new Document("membersMark", markDoc));
//			FindOneAndUpdateOptions options = new FindOneAndUpdateOptions();
//			filter = Filters.and(Filters.eq("groupId", groupId), Filters.eq("uid", uid));
//			coll.findOneAndUpdate(filter, update, options.upsert(true));
//		}
//		log.info("end...,groupId = [{}],uid = [{}],markedUid = [{}],mark = [{}]", groupId, uid, markedUid, mark);
//	}

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
		coll.updateOne(filter, update);
	}
	
	/**
	 * 修改群组成员消息initSeq
	 * @param appId
	 * @param groupId
	 * @param uid
	 * @param initSeq
	 */
	public void updateMemberInitSeq(String appId, String groupId, long uid, long initSeq) {
		log.info("[updateMemberInitSeq] appId=[{}],groupId=[{}],uid=[{}],initSeq=[{}]", appId, groupId, uid, initSeq);
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
		Bson filter = Filters.and(Filters.eq("groupId", groupId), Filters.eq("uid", uid));
		Document upDoc = new Document();
		upDoc.append("initSeq", initSeq);
		upDoc.append("updateTime", System.currentTimeMillis());
		Document update = new Document("$set", upDoc);
		coll.updateOne(filter, update);
	}

	/**
	 * 获取群组所有成员
	 * @param appId
	 * @param groupId
	 * @return
	 */
	public List<GroupMember> listGroupMember(String appId, String groupId) {
		log.info("[listGroupMember] appId=[{}],groupId=[{}]", appId, groupId);
		List<GroupMember> list = new ArrayList<GroupMember>();
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
		Bson where = Filters.eq("groupId", groupId);
		MongoCursor<Document> docs = coll.find(where).iterator();
		while (docs.hasNext()) {
			Document doc = docs.tryNext();
			GroupMember member = (GroupMember) BeanTransUtils.document2Bean(doc, GroupMember.class);
			list.add(member);
		}
		log.info("[listGroupMember] appId=[{}],groupId=[{}],list size=[{}]", appId, groupId, list.size());
		return list;
	}

//	/**
//	 * 获取群组管理员
//	 * 
//	 * @param groupId
//	 * @param status
//	 * @return
//	 */
//	public List<GroupMember> listGroupManager(String groupId, int status) {
//		log.info("start...,groupId = [{}]", groupId);
//		List<GroupMember> list = new ArrayList<GroupMember>();
//		MongoCollection<Document> coll = this.getCollection(dbName, collName);
//		Bson where = Filters.and(Filters.eq("groupId", groupId), Filters.eq("status", status),
//				Filters.gte("identity", Constant.GROUP_MEMEBER_IDENTITY.CREATOR));
//		MongoCursor<Document> docs = coll.find(where).iterator();
//		while (docs.hasNext()) {
//			Document doc = docs.tryNext();
//			GroupMember member = (GroupMember) BeanTransUtils.document2Bean(doc, GroupMember.class);
//			list.add(member);
//		}
//		log.info("end...,groupId = [{}],result list size = [{}]", groupId, list.size());
//		return list;
//	}

	/**
	 * 根据群组id和用户id获取群组用户信息
	 * @param appId
	 * @param groupId
	 * @param uid
	 * @return
	 */
	public GroupMember getMemberByGroupIdAndUid(String appId, String groupId, long uid) {
		log.info("[getMemberByGroupIdAndUid] appId=[{}],groupId=[{}],uid=[{}]", appId, groupId, uid);
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
		Bson where = Filters.and(Filters.eq("groupId", groupId), Filters.eq("uid", uid));
		MongoCursor<Document> docs = coll.find(where).iterator();
		while (docs.hasNext()) {
			Document doc = docs.next();
			GroupMember member = (GroupMember) BeanTransUtils.document2Bean(doc, GroupMember.class);
			log.info("[getMemberByGroupIdAndUid] appId=[{}],groupId=[{}],uid=[{}]", appId, groupId, uid);
			return member;
		}
		log.info("[getMemberByGroupIdAndUid] appId=[{}],return null,groupId=[{}],uid=[{}]", appId, groupId, uid);
		return null;
	}

	/**
	 * 获取成员群组信息
	 * @param appId
	 * @param uid
	 * @return
	 */
	public List<GroupMember> listMemberByUid(String appId, long uid) {
		log.info("[listMemberByUid] appId=[{}],uid=[{}]", appId, uid);
		List<GroupMember> list = new ArrayList<GroupMember>();
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
		//Bson where = Filters.and(Filters.eq("uid", uid), Filters.gte("updateTime", time));
		Bson where = Filters.eq("uid", uid);
		MongoCursor<Document> docs = coll.find(where).iterator();
		while (docs.hasNext()) {
			Document doc = docs.next();
			GroupMember member = (GroupMember) BeanTransUtils.document2Bean(doc, GroupMember.class);
			list.add(member);
		}
		log.info("[listMemberByUid] appId=[{}],uid=[{}],result list size=[{}]", appId, uid, list.size());
		
		return list;
	}
	
	/**
	 * 从redis获取群组成员id；去除消息fromUid
	 * @param appId
	 * @param groupId
	 * @return
	 */
	public List<Long> listMemberUids(String appId, String groupId) {
		log.info("[listMembersFromRdis] appId=[{}],groupId=[{}]", appId, groupId);
		List<Long> list = new ArrayList<Long>();
		JedisCluster cluster = JedisUtils.getInstance().getJedisCluster();
		String key = appId + "_" + groupId + "_members";
		Map<String, String> map = cluster.hgetAll(key);
		if(map == null || map.isEmpty()) {
			List<GroupMember> members = listGroupMember(appId, groupId);
			if(members != null) {
				for(GroupMember member : members) {
					//long uid = member.getUserId();
					long uid = member.getUid();
					list.add(uid);
				}
			}
			return list;
		}
		for(String uid : map.keySet()) {
			long luid = Long.valueOf(uid);
			list.add(luid);
		}
		
		return list;
	}
}
