package cn.com.gome.logic.dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.model.Group;
import cn.com.gome.logic.model.notice.SaveNoticeMsg;
import cn.com.gome.logic.utils.BeanTransUtils;
import cn.com.gome.logic.utils.CheckClientTypeUtils;
import cn.com.gome.logic.utils.StringUtils;

public class NoticeMsgDao extends BaseDao{
	private static final Logger log = LoggerFactory.getLogger(NoticeMsgDao.class);
	private final static String collName = "t_notice_msg_";
		
	/**
	 * 获取功能消息
	 * @param appId
	 * @param uid
	 * @param clientId
	 * @return
	 */
	public List<SaveNoticeMsg> listNotice(String appId, long uid, byte clientId, int traceId) {
		List<SaveNoticeMsg> list = new ArrayList<SaveNoticeMsg>();
		try {
			log.info("[listNotice] tranceId=[{}], appId=[{}],uid=[{}],clientId=[{}]", traceId, appId, uid, clientId);
			Bson orBson = this.getWether((int)clientId);
			if(orBson == null) {
				log.info("[listNotice] tranceId=[{}], appId=[{}],uid=[{}],clientId=[{}], platforms length=0", traceId, appId, uid, clientId);
				return list;
			}
			String[] names = getDBAndTableName(appId, uid, traceId);
			log.info("[listNotice] tranceId=[{}], dbName=[{}],tableName=[{}]", traceId, names[0], names[1]);
			MongoCollection<Document> coll = this.getCollection(names[0], names[1]);
			Bson andBaon = Filters.eq("toUid", uid);
			Bson where = Filters.and(andBaon, orBson);
			Bson sort = new BasicDBObject("sendTime", 1);
			MongoCursor<Document> cursor = coll.find(where).sort(sort).iterator();
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				SaveNoticeMsg msg = (SaveNoticeMsg) BeanTransUtils.document2Bean(doc, SaveNoticeMsg.class);
				//log.info("[listNotice] tranceId=[{}], dbName=[{}],tableName=[{}], msgId=[{}]", traceId, names[0], names[1], msg.getMsgId());
				list.add(msg);
			}
		} catch(Exception e) {
			log.error("[listNotice]: ", e);
		}
		
		
		return list;
	}
	
	/**
	 * 更新用户平台信息--剔除已获取消息平台
	 * @param appId
	 * @param uid
	 * @param groupId
	 * @param msgId
	 */
	public void updatePlatform(String appId, long uid,String msgId, int newPlatform, int traceId) {
		String[] names = getDBAndTableName(appId, uid, traceId);
		log.info("[updatePlatform] tranceId=[{}],appId=[{}],uid=[{}],msgId=[{}],newPlatform=[{}],dbName=[{}],tableName=[{}]",
				traceId, appId, uid, msgId, newPlatform, names[0], names[1]);
		BasicDBObject filter = new BasicDBObject();
		filter.put("msgId", msgId);
		filter.put("toUid", uid);
		Document doc = new Document();
//		if(newPlatform > 0){
			doc.put("platform", newPlatform);
//		}else{
//			log.info("[updatePlatform] ERROR newPlatform=[{}]", newPlatform);
//		}
		MongoCollection<Document> collection = getCollection(names[0], names[1]);
		collection.updateMany(filter, new Document("$set", doc));
	}
	
	/**
	 * 获取登录平台信息--删除更新功能性消息时使用(msgId 已知)
	 * @param appId
	 * @param uid
	 * @param groupId
	 * @param msgId
	 */
	public List<Integer> getPlatform(String appId, long uid,String msgId, int traceId) {
		List<Integer> list = new ArrayList<Integer>();
		String[] names = getDBAndTableName(appId, uid, traceId);
		log.info("[getPlatform] tranceId=[{}],appId=[{}],uid=[{}],msgId=[{}],dbName=[{}],tableName=[{}]",
				traceId, appId, uid, msgId, names[0], names[1]);
		BasicDBObject filter = new BasicDBObject();
		filter.put("msgId", msgId);
		filter.put("toUid", uid);
		MongoCollection<Document> collection = getCollection(names[0], names[1]);
		MongoCursor<Document> cursor = collection.find(filter).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			SaveNoticeMsg msg = (SaveNoticeMsg) BeanTransUtils.document2Bean(doc, SaveNoticeMsg.class);
			int platform = msg.getPlatform();
			if(platform <= 0) {
				continue;
			}
			list.add(platform);
		}
		
		return list;
	}
	
	/**
	 * 根据appId,groupId,msgId修改消息状态    修改 msgStatus为1
	 * msgStatus -->  0:正常、1:撤回、2:删除
	 * 此方法只提供撤回修改
	 * @param appId
	 * @param groupId
	 * @param msgId
	 */
	public void ModifyMsgStatusRevoke(String appId, String groupId, String msgId, int traceId) {		
		String[] names = getDBAndTableName(appId, groupId, traceId);
		log.info("[ModifyMsgStatusRevoke] RevokeMsgWorker tranceId=[{}],appId=[{}],msgId=[{}],dbName=[{}],tableName=[{}]",
				traceId, appId, msgId, names[0], names[1]);
		//设置条件
		BasicDBObject filter = new BasicDBObject();
        filter.put("msgId", msgId);
        //更新字段
        Document newdoc = new Document();
        newdoc.put("msgStatus", 1);
        this.update(names[0], names[1], filter, newdoc);
	}
	
	/**
	 * 保存功能性消息
	 * @param saveNoticeMsg, appId, uid
	 */
	public void saveNoticeMsg(SaveNoticeMsg saveNoticeMsg, String appId, long uid, int traceId) {
		Document document = BeanTransUtils.bean2Document(saveNoticeMsg);
		String[] DBAndTableName = getDBAndTableName(appId, uid, traceId);
		this.insert(DBAndTableName[0], DBAndTableName[1], document);
		log.info("[saveNoticeMsg] traceId=[{}], DBName=[{}], TableName=[{}]:",traceId,  DBAndTableName[0], DBAndTableName[1]);
	}
	
	/**
	 * 根据group计算库名和表名
	 * @param groupId
	 * @return
	 */
	private String[] getDBAndTableName(String appId, String groupId, int traceId) {
		log.info("[getDBAndTableName] traceId=[{}], appId=[{}],groupId=[{}]",traceId, appId, groupId);
		String[] arr = new String[2];
		int hashValue = StringUtils.FNVHash1(groupId);
		arr[0] = dbMsgName + "_" + appId.trim() + "_" + hashValue % Global.MSG_DB_MODULO;
		hashValue = StringUtils.SDBMHash(groupId);
		arr[1] = collName + hashValue % Global.MSG_TABLE_MODULO;

		return arr;
	}
	
	/**
	 * 根据uid计算库名和表名
	 * @param appId,uid
	 * @return
	 */
	private String[] getDBAndTableName(String appId, long uid, int traceId) {
		log.info("[getDBAndTableName] traceId=[{}], appId=[{}],uid=[{}]", traceId, appId, uid);
		String[] arr = new String[2];
		int hashValue = StringUtils.FNVHash1("" + uid);
		arr[0] = dbMsgName + "_" + appId + "_" + hashValue % Global.MSG_DB_MODULO;
		hashValue = StringUtils.SDBMHash("" + uid);
		arr[1] = collName + hashValue % Global.MSG_TABLE_MODULO;
		return arr;
	}
	
	/**
	 * 根据appId,groupId获取群主userId
	 * 
	 * @return userId
	 */
	public long getToUid(String appId, String groupId) {
		long userId = 0;
		BasicDBObject filter = new BasicDBObject();
		filter.put("groupId", groupId);
		MongoCollection<Document> collection = getCollection("db_im_" + appId, "t_group");
		MongoCursor<Document> coursor = collection.find(filter).iterator();
		while (coursor.hasNext()) {
			Document item = coursor.next();
			Group group = (Group) BeanTransUtils.document2Bean(item, Group.class);
			userId = group.getUserId();
		}
		return userId;
	}
	
	/**
	 * 
	 * @param requestPlatform
	 * @return
	 */
	private Bson getWether(int clientId) {
		int platform = 0;
		if (CheckClientTypeUtils.clientType_mobile(clientId)) {
			platform = 1;
		} else if (CheckClientTypeUtils.clientType_pc(clientId)) {
			platform = 2;
		} else if (clientId == 30) {// Web端--30:Web
			platform = 4;
		} else if (clientId == 40) {// H5端--40:H5
			platform = 8;
		}
		
		if (platform == 1) {// 1-M-移动端
//			int[] mobile = new int[] { 1, 3, 5, 7, 9, 11, 13, 15 };
			Bson orBson = Filters.or(Filters.eq("platform", 1), Filters.eq("platform", 3), Filters.eq("platform", 5)
					,Filters.eq("platform", 7), Filters.eq("platform", 9), Filters.eq("platform", 11)
					,Filters.eq("platform", 13), Filters.eq("platform", 15));
			
			return orBson;
		} else if (platform == 2) {// 2-PC-电脑端
//			int[] pc = new int[] { 2, 3, 6, 7, 10, 11, 14, 15 };
			Bson orBson = Filters.or(Filters.eq("platform", 2), Filters.eq("platform", 3), Filters.eq("platform", 6)
					,Filters.eq("platform", 7), Filters.eq("platform", 10), Filters.eq("platform", 11)
					,Filters.eq("platform", 14), Filters.eq("platform", 15));
			
			return orBson;
		} else if (platform == 4) {// 4-Web-网页端
//			int[] web = new int[] { 4, 5, 6, 7, 12, 13, 14, 15 };
			Bson orBson = Filters.or(Filters.eq("platform", 4), Filters.eq("platform", 5), Filters.eq("platform", 6)
					,Filters.eq("platform", 7), Filters.eq("platform", 12), Filters.eq("platform", 13)
					,Filters.eq("platform", 14), Filters.eq("platform", 15));
			
			return orBson;
		} else if (platform == 8) {// 8-H5-H5端
//			int[] pad = new int[] { 8, 9, 10, 11, 12, 13, 14, 15 };
			Bson orBson = Filters.or(Filters.eq("platform", 8), Filters.eq("platform", 9), Filters.eq("platform", 10)
					,Filters.eq("platform", 11), Filters.eq("platform", 12), Filters.eq("platform", 13)
					,Filters.eq("platform", 14), Filters.eq("platform", 15));
			
			return orBson;
		}

		return null;
	}
}
