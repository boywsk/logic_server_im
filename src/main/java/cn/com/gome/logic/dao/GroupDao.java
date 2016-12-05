package cn.com.gome.logic.dao;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.model.Group;
import cn.com.gome.logic.model.GroupMember;
import cn.com.gome.logic.model.GroupMsg;
import cn.com.gome.logic.utils.BeanTransUtils;
import cn.com.gome.logic.utils.JedisUtils;

import com.google.common.base.Strings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

/**
 * 群组数据库操作层
 */
public class GroupDao extends BaseDao {
	private static final Logger log = LoggerFactory.getLogger(GroupDao.class);

	//private final static String dbName = "db_im";
	private final static String collName = "t_group";
	private final static String member_collName = "t_group_member";

	/**
	 * 保存群组信息
	 * 
	 * @param group
	 */
	public void save(String appId, Group group) {
		log.info("[save] appId = [{}],groupId=[{}]", appId, group.getGroupId());
		Document doc = BeanTransUtils.bean2Document(group);
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
		coll.insertOne(doc);
	}

	/**
	 * 保存最后一条消息，并递增seqId
	 * @param appId
	 * @param msg
	 * @return
	 */
	public Group incGroupSeq(String appId, GroupMsg msg) {
		log.info("[incGroupSeq] appId=[{}],groupId=[{}],senderId is=[{}],msgId=[{}]", appId, msg.getGroupId(), msg.getSenderId(),
				msg.getMsgId());
		if (msg.getSendTime() <= 0) {
			msg.setSendTime(System.currentTimeMillis());
		}
		String groupId = msg.getGroupId();
		if(Strings.isNullOrEmpty(groupId)) {
			log.error("groupId is null or empty; appId=[{}]", appId);
			return null;
		}
		JedisUtils util = JedisUtils.getInstance();
		String key = appId + "_groupSeqId" + "_" + groupId;
		long seqId = util.getJedisCluster().incr(key);
		
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
//		Bson where = Filters.eq("groupId", msg.getGroupId());
//		Document doc = new Document("$inc", new Document().append("seq", 1));
//		Document gDoc = coll.findOneAndUpdate(where, doc);
		Bson where = Filters.eq("groupId", groupId);
		Document gDoc = coll.find(where).first();
		Group group = new Group();
		//群组不存在保存群组
		if(null == gDoc) {
			msg.setMsgSeqId(seqId);
			int groupType = msg.getGroupType();
			log.info("[incGroupSeq] not exit;groupId=[{}],groupType=[{}]", msg.getGroupId(), groupType);
			
			//单聊保存成员;客服
			if(groupType == Constant.CHAT_TYPE.SINGLE.value || groupType == Constant.CHAT_TYPE.CUSTOMER.value) {
				group.setGroupId(msg.getGroupId());
				group.setType(msg.getGroupType());
				group.setSeq(seqId);
				group.setIsDele(0);
				group.setCreateTime(System.currentTimeMillis());
				group.setUpdateTime(System.currentTimeMillis());
				group.setLastMsg(msg.toString());
				this.save(appId, group);
				groupId = msg.getGroupId();
				String[] arr = groupId.split("_");
				List<Document> list = new ArrayList<Document>();
				//单独处理商家客服
				if(groupType == Constant.CHAT_TYPE.CUSTOMER.value) {
					log.info("[incGroupSeq] CUSTOMER groupType=[{}],groupId=[{}]", groupType, groupId);
					int length = arr.length;
					if(length > 2) {
						length = 2;
						for(int i = 0; i < length; i++) {
							String uid = arr[i];
							GroupMember member = new GroupMember();
							member.setGroupId(msg.getGroupId());
							member.setInitSeq(0);
							member.setReadSeq(0);
							member.setUid(Long.valueOf(uid));
							member.setJoinTime(System.currentTimeMillis());
							member.setUpdateTime(System.currentTimeMillis());
							Document document = BeanTransUtils.bean2Document(member);
							list.add(document);
						}
					}
				} else {
					log.info("[incGroupSeq] SINGLE groupType=[{}],groupId=[{}]", groupType, groupId);
					for(String uid : arr) {
						GroupMember member = new GroupMember();
						member.setGroupId(msg.getGroupId());
						member.setInitSeq(0);
						member.setReadSeq(0);
						member.setUid(Long.valueOf(uid));
						member.setJoinTime(System.currentTimeMillis());
						member.setUpdateTime(System.currentTimeMillis());
						Document document = BeanTransUtils.bean2Document(member);
						list.add(document);
					}
				}
				if(!list.isEmpty()) {
					MongoCollection<Document> coll2 = this.getAppCollection(appId, member_collName);
					coll2.insertMany(list);
				}
			}
		} else {
			group = (Group) BeanTransUtils.document2Bean(gDoc, Group.class);
			group.setSeq(seqId);
			msg.setMsgSeqId(seqId);
			Document upDoc = new Document();
			upDoc.append("seq", seqId);
			upDoc.append("lastMsg", msg.toString());
			upDoc.append("updateTime", System.currentTimeMillis());
			coll.updateOne(where, new Document("$set", upDoc));
		}
		log.info("[incGroupSeq] groupId=[{}],senderId=[{}],msgId=[{}],seqId=[{}]", msg.getGroupId(), msg.getSenderId(),
				msg.getMsgId(), msg.getMsgSeqId());
		
		return group;
	}

	/**
	 * 根据groupId获取group
	 * @param appId
	 * @param groupId
	 * @return
	 */
	public Group getGroupById(String appId, String groupId) {
		log.info("[getGroupById] appId=[{}],groupId=[{}]", appId, groupId);
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
		Bson where = Filters.eq("groupId", groupId);
		Document doc = coll.find(where).first();
		Group group = (Group) BeanTransUtils.document2Bean(doc, Group.class);
		if(group != null) {
			JedisUtils util = JedisUtils.getInstance();
			String key = appId + "_groupSeqId" + "_" + groupId;
			long seqId = 0L;
			String value = util.getJedisCluster().get(key);
			if(value != null) {
				seqId = Long.valueOf(value);
			}
			group.setSeq(seqId);
		}

		return group;
	}

//	/**
//	 * 根据groupId获取group
//	 * @param appId
//	 * @param groupId
//	 * @param isDele
//	 * @return
//	 */
//	public Group getGroup(String appId, String groupId) {
//		log.info("appId=[{}],groupId = [{}]", appId, groupId);
//		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
//		Bson where = Filters.eq("groupId", groupId);
//		Document doc = coll.find(where).first();
//		Group group = (Group) BeanTransUtils.document2Bean(doc, Group.class);
//
//		return group;
//	}

	/**
	 * 根据groupId列表最后一次修改时间获取group列表
	 * @param appId
	 * @param groupIds
	 * @param time
	 * @return
	 */
	public List<Group> listGroup(String appId, List<String> groupIds, long time) {
		List<Group> groups = new ArrayList<Group>();

		log.info("[listGroup] appId=[{}],groupIds size=[{}],time=[{}]", appId, groupIds.size(), time);
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
		Bson where = Filters.and(Filters.in("groupId", groupIds), 
				Filters.gte("updateTime", time), Filters.eq("isDel", 0));
		MongoCursor<Document> cursor = coll.find(where).iterator();
		while (cursor != null && cursor.hasNext()) {
			Document doc = cursor.tryNext();
			Group group = (Group) BeanTransUtils.document2Bean(doc, Group.class);
			if(group != null) {
				JedisUtils util = JedisUtils.getInstance();
				String key = appId + "_groupSeqId" + "_" + group.getGroupId();
				long seqId = 0L;
				String value = util.getJedisCluster().get(key);
				if(value != null) {
					seqId = Long.valueOf(value);
				}
				group.setSeq(seqId);
			}
			groups.add(group);
		}
		log.info("[listGroup] groups size=[{}]", groups.size());

		return groups;
	}

	/**
	 * 根据groupId获取group
	 * @param appId
	 * @param groupId
	 * @param time
	 * @return
	 */
	public Group getGroupById(String appId, String groupId, long time) {
		log.info("[getGroupById] appId=[{}],groupId=[{}],time=[{}]", appId, groupId, time);
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
		Bson where = Filters.and(Filters.eq("groupId", groupId), Filters.gte("updateTime", time));
		Document doc = coll.find(where).first();
		Group group = (Group) BeanTransUtils.document2Bean(doc, Group.class);
		if(group != null) {
			JedisUtils util = JedisUtils.getInstance();
			String key = appId + "_groupSeqId" + "_" + groupId;
			long seqId = 0L;
			String value = util.getJedisCluster().get(key);
			if(value != null) {
				seqId = Long.valueOf(value);
			}
			group.setSeq(seqId);
		}
		return group;

	}

	/**
	 * 根据群组id列表获取群组
	 * @param appId
	 * @param groupIds
	 * @return
	 */
	public List<Group> listGroupByIds(String appId, List<String> groupIds) {
		log.info("[listGroupByIds] appId=[{}],groupId size = [{}]", appId, groupIds == null ? 0 : groupIds.size());
		List<Group> list = new ArrayList<Group>();
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
		Bson where = Filters.in("groupId", groupIds);
		MongoCursor<Document> cursor = coll.find(where).iterator();
		while (cursor != null && cursor.hasNext()) {
			Document doc = cursor.tryNext();
			Group group = (Group) BeanTransUtils.document2Bean(doc, Group.class);
			if(group != null) {
				JedisUtils util = JedisUtils.getInstance();
				String key = appId + "_groupSeqId" + "_" + group.getGroupId();
				long seqId = 0L;
				String value = util.getJedisCluster().get(key);
				if(value != null) {
					seqId = Long.valueOf(value);
				}
				group.setSeq(seqId);
			}
			list.add(group);
		}
		log.info("[listGroupByIds] result group list size = [{}]", list.size());
		return list;
	}

	/**
	 * 获取某用户群组列表
	 * @param appId
	 * @param uid
	 * @param updateTime
	 * @return
	 */
	public List<Group> listGroupByUid(String appId, long uid, long updateTime) {
		log.info("[listGroupByUid] appId=[{}],uid=[{}],updateTime=[{}]", appId, uid, updateTime);
		List<Group> list = new ArrayList<Group>();
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
		Bson where = Filters.and(Filters.eq("uid", uid), Filters.gte("updateTime", updateTime));
		MongoCursor<Document> cursor = coll.find(where).iterator();
		while (cursor != null && cursor.hasNext()) {
			Document doc = cursor.tryNext();
			Group group = (Group) BeanTransUtils.document2Bean(doc, Group.class);
			if(group != null) {
				JedisUtils util = JedisUtils.getInstance();
				String key = appId + "_groupSeqId" + "_" + group.getGroupId();
				long seqId = 0L;
				String value = util.getJedisCluster().get(key);
				if(value != null) {
					seqId = Long.valueOf(value);
				}
				group.setSeq(seqId);
			}
			list.add(group);
		}
		log.info("[listGroupByUid] appId=[{}],uid=[{}],updateTime=[{}],group size=[{}]", appId, uid, updateTime, list.size());
		
		return list;
	}
	
	/**
	 * 获取系统消息群组
	 * @param appId
	 * @param updateTime
	 * @return
	 */
	public List<Group> listSystemGroup(String appId, long updateTime) {
		log.info("[listSystemGroup] appId=[{}],updateTime=[{}]", appId, updateTime);
		List<Group> list = new ArrayList<Group>();
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
		Bson where = Filters.and(Filters.eq("type", Constant.CHAT_TYPE.SYS.value), Filters.gte("updateTime", updateTime));
		MongoCursor<Document> cursor = coll.find(where).iterator();
		while (cursor != null && cursor.hasNext()) {
			Document doc = cursor.tryNext();
			Group group = (Group) BeanTransUtils.document2Bean(doc, Group.class);
			if(group != null) {
				JedisUtils util = JedisUtils.getInstance();
				String key = appId + "_groupSeqId" + "_" + group.getGroupId();
				long seqId = 0L;
				String value = util.getJedisCluster().get(key);
				if(value != null) {
					seqId = Long.valueOf(value);
				}
				group.setSeq(seqId);
			}
			list.add(group);
		}
		log.info("[listSystemGroup] appId=[{}],updateTime=[{}],group size=[{}]", appId, updateTime, list.size());
		
		return list;
	}
}
