package cn.com.gome.logic.dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.gome.logic.global.Constant;
import cn.com.gome.logic.model.User;
import cn.com.gome.logic.utils.BeanTransUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

/**
 * 数据库用户信息
 */
public class UserDao extends BaseDao {
	Logger log = LoggerFactory.getLogger(UserDao.class);
	
	//private final static String dbName = "db_im";
	private final static String collName = "t_user_info";
	
	/**
	 * 修改用户设备类型和设备标识
	 * @param appId
	 * @param uid
	 * @param clientId
	 * @param clientType
	 * @param apnsToken
	 */
	public void updateUser(String appId, long uid, byte clientId, byte clientType, String apnsToken, String deviceId) {
		log.info("[updateUser] appId=[{}],uid=[{}],clientId=[{}], clientType=[{}], apnsToken=[{}]", appId, uid, clientId, clientType, apnsToken);
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
		Bson where = Filters.eq("uid", uid);
		Document upDoc = new Document();
		upDoc.append("clientId", clientId);
		upDoc.append("clientType", clientType);
		//if(!StringUtils.isEmpty(apnsToken)) {
		upDoc.append("apnsToken", apnsToken);
		upDoc.append("deviceId", deviceId);
		//}
		upDoc.append("updateTime", System.currentTimeMillis());
		coll.updateOne(where, new Document("$set", upDoc));
	}
	
	/**
	 * 修改登录token
	 * @param appId
	 * @param uid
	 * @param apnsToken
	 */
	public void updateApnsToken(String appId, long uid, String apnsToken) {
		log.info("[updateApnsToken] appId=[{}],uid=[{}],apnsToken=[{}]", appId, uid, apnsToken);
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
		Bson where = Filters.eq("uid", uid);
		Document upDoc = new Document();
		upDoc.append("apnsToken", apnsToken);
		upDoc.append("updateTime", System.currentTimeMillis());
		coll.updateOne(where, new Document("$set", upDoc));
	}
	
	/**
	 * 根据appId，uid获取用户信息
	 * @param appId
	 * @param uid
	 * @return
	 */
	public User getUser(String appId, long uid) {
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
//		Bson where = Filters.and(Filters.eq("uid", uid), Filters.eq("clientId", Constant.DEVICE_TYPE.IOS.value));
		Bson where = Filters.eq("uid", uid);
		Document doc = coll.find(where).first();
		User user = (User)BeanTransUtils.document2Bean(doc, User.class);
		log.info("[getUser] appId=[{}],uid=[{}]", appId, uid);
		return user;
	}
	
	/**
	 * 根据appId，uid获取用户信息
	 * @param appId
	 * @param uid
	 * @return
	 */
	public User getIOSUser(String appId, long uid) {
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
		Bson where = Filters.and(Filters.eq("uid", uid), Filters.eq("clientId", Constant.DEVICE_TYPE.IOS.value));
		//Bson where = Filters.eq("uid", uid);
		Document doc = coll.find(where).first();
		User user = (User)BeanTransUtils.document2Bean(doc, User.class);
		log.info("[getUser] appId=[{}],uid=[{}]", appId, uid);
		return user;
	}
	
	/**
	 * 正排序分页获取用户信息
	 * @param lastUid；获取的列表所有的用户id都大于该id
	 * @param pageSize
	 * @return
	 */
	public List<User> listUserForPage(String appId, long lastUid, int pageSize) {
		log.info("[listUserForPage] appId=[{}],lastUid=[{}],pageSize=[{}]", appId, lastUid, pageSize);
		MongoCollection<Document> coll = this.getAppCollection(appId, collName);
		List<User> users = new ArrayList<User>();
		Bson where = Filters.gt("uid", lastUid);
		Bson sort = new BasicDBObject("uid", 1);
		MongoCursor<Document> cursor = coll.find(where).sort(sort).limit(pageSize).iterator();
		while(cursor.hasNext()) {
			Document doc = cursor.tryNext();
			User user = (User)BeanTransUtils.document2Bean(doc, User.class);
			users.add(user);
		}
		return users;
	}
}
