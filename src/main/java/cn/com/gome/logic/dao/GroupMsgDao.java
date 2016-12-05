package cn.com.gome.logic.dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.model.Group;
import cn.com.gome.logic.model.GroupMsg;
import cn.com.gome.logic.utils.BeanTransUtils;
import cn.com.gome.logic.utils.StringUtils;

/**
 * 数据库消息相关操作
 */
public class GroupMsgDao extends BaseDao {
	private static final Logger log = LoggerFactory.getLogger(GroupMsgDao.class);
	// private final static String dbName = "db_msg_";
	private final static String collName = "t_group_msg_";

	/**
	 * 保存消息
	 * 
	 * @param msg
	 */
	public void saveMsg(String appId, GroupMsg msg) {
		log.info("[saveMsg] appId=[{}],msgId=[{}],senderId=[{}],senderName=[{}],groupId=[{}]", appId, msg.getMsgId(),
				msg.getSenderId(), msg.getSenderName(), msg.getGroupId());
		String[] names = getDBAndTableName(appId, msg.getGroupId());
		log.info("[saveMsg] dbName=[{}],tableName=[{}]", names[0], names[1]);
		if (msg.getSendTime() <= 0) {
			msg.setSendTime(System.currentTimeMillis());
		}
		Document doc = BeanTransUtils.bean2Document(msg);
		this.insert(names[0], names[1], doc);
		log.info("[saveMsg] msgId=[{}],senderId=[{}],groupId=[{}]", msg.getMsgId(), msg.getSenderId(),
				msg.getGroupId());
	}

	/**
	 * 分页获取聊天消息
	 * 
	 * @param groupId
	 * @param seqId
	 * @param size
	 * @return
	 */
	public List<GroupMsg> listGroupMsg(String appId, String groupId, long seqId, int size) {
		log.info("[listGroupMsg] appId=[{}],groupId=[{}],seqId=[{}],pageSize=[{}]", appId, groupId, seqId, size);
		String[] names = getDBAndTableName(appId, groupId);
		log.info("[listGroupMsg] dbName=[{}],tableName=[{}]", names[0], names[1]);
		List<GroupMsg> list = new ArrayList<GroupMsg>();
		if (size <= 0) {
			return list;
		}

		MongoCollection<Document> coll = this.getCollection(names[0], names[1]);
		Bson where = Filters.and(Filters.eq("groupId", groupId), Filters.lte("msgSeqId", seqId));
		Bson sort = new BasicDBObject("msgSeqId", -1);
		MongoCursor<Document> cursor = coll.find(where).sort(sort).limit(size).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.tryNext();
			GroupMsg msg = (GroupMsg) BeanTransUtils.document2Bean(doc, GroupMsg.class);
			list.add(msg);
		}
		return list;
	}

	/**
	 * 获取单条消息
	 * 
	 * @param appId
	 * @param uid
	 * @param groupId
	 * @param msgId
	 * @return
	 */
	public GroupMsg getMsg(String appId, long uid, String groupId, String msgId, int traceId) {
		log.info("[listMsgReader] appId=[{}],groupId=[{}],uid=[{}],msgId=[{}]", appId, groupId, uid, msgId);
		String[] names = getDBAndTableName(appId, groupId);
		log.info("[listMsgReader] dbName=[{}],tableName=[{}]", names[0], names[1]);
		MongoCollection<Document> coll = this.getCollection(names[0], names[1]);
		Bson filter = Filters.and(Filters.eq("groupId", groupId), Filters.eq("msgId", msgId));
		Document doc = coll.find(filter).first();
		if (doc != null) {
			GroupMsg msg = (GroupMsg) BeanTransUtils.document2Bean(doc, GroupMsg.class);
			return msg;
		}
		return null;
	}

	/**
	 * 根据group计算库名和表名
	 * 
	 * @param groupId
	 * @return
	 */
	private String[] getDBAndTableName(String appId, String groupId) {
		log.info("[getDBAndTableName] appId=[{}],groupId=[{}]", appId, groupId);
		String[] arr = new String[2];
		int hashValue = StringUtils.FNVHash1(groupId);
		arr[0] = dbMsgName + "_" + appId.trim() + "_" + hashValue % Global.MSG_DB_MODULO;
		hashValue = StringUtils.SDBMHash(groupId);
		arr[1] = collName + hashValue % Global.MSG_TABLE_MODULO;
		return arr;
	}

	/**
	 * 根据appId,groupId,msgId修改消息状态 修改 msgStatus为1 msgStatus --> 0:正常、1:撤回、2:删除
	 * 此方法只提供撤回修改
	 * 
	 * @param appId
	 * @param groupId
	 * @param msgId
	 */
	public void modifyMsgStatusRevoke(String appId, String groupId, String msgId, int traceId) {
		String[] names = getDBAndTableName(appId, groupId);
		log.info(
				"[ModifyMsgStatusRevoke-AAA] RevokeMsgWorker tranceId=[{}],appId=[{}],msgId=[{}],dbName=[{}],tableName=[{}]",
				traceId, appId, msgId, names[0], names[1]);
		MongoCollection<Document> coll = this.getCollection(names[0], names[1]);
		BasicDBObject filter = new BasicDBObject();	// 设置条件
		filter.put("msgId", msgId);
		Document upDoc = new Document();			// 更新字段
		upDoc.put("msgStatus", 1);
		Document update = new Document("$set", upDoc);
		/*log.info("[ModifyMsgStatusRevoke-AAA] RevokeMsgWorker tranceId=[{}], filter=[{}], update=[{}]",
				traceId, JSON.toJSONString(filter), JSON.toJSONString(update));*/
		coll.updateMany(filter, update);
	}

	/**
	 * 根据appId,groupId,msgId修改消息状态 修改t_group文档中的msgStatus为1 msgStatus -->
	 * 0:正常、1:撤回、2:删除 此方法只提供撤回修改
	 * 
	 * @param appId
	 * @param groupId
	 * @param msgId
	 */
	public void judgeAndModify(String appId, String groupId, String msgId, int traceId) {
		log.info("[JudgeAndModify-AAA] tranceId=[{}],appId=[{}],msgId=[{}],dbName=[{}],tableName=[{}]", traceId, appId,
				msgId, "db_im_" + appId, "t_group");
		MongoCollection<Document> coll = this.getAppCollection(appId, "t_group");
		Bson where = Filters.eq("groupId", groupId);
		Document gDoc = coll.find(where).first();
		if (null != gDoc) {// 判断是否有群组信息
			Group group = (Group) BeanTransUtils.document2Bean(gDoc, Group.class);
			String lastMsg = group.getLastMsg();
			GroupMsg groupMsg = JSON.parseObject(lastMsg, GroupMsg.class);// 将建json对象转换为Person对象
			if (msgId.equals(groupMsg.getMsgId())) {// 判断是否是最后一条消息
				long systemTime = System.currentTimeMillis();
				groupMsg.setMsgStatus(1);
				groupMsg.setSendTime(systemTime);

				Document upDoc = new Document();
				upDoc.append("lastMsg", groupMsg.toString());
				upDoc.append("updateTime", systemTime);
				coll.updateOne(where, new Document("$set", upDoc));
			}
		} else {
			log.info(
					"[JudgeAndModify] tranceId=[{}],appId=[{}],msgId=[{}],dbName=[{}],tableName=[{}], No exist the groupId [{}] ",
					traceId, appId, msgId, "db_im_" + appId, "t_group", groupId);
		}
	}
}
