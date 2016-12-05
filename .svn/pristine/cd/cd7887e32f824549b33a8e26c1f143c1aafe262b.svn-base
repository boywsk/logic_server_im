package cn.com.gome.logic.dao;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import cn.com.gome.logic.global.Global;
import cn.com.gome.logic.utils.StringUtils;

/**
 * BaseDAO
 */
public class BaseDao {

	static Logger log = LoggerFactory.getLogger(BaseDao.class);
	
	protected final static String dbName = Global.DB_NAME;
	protected final static String dbMsgName = Global.DB_MSG_NAME;
	
	private static MongoClient client = null;
	// private static String COLLECTIONS_NAME = "offline";

	static {
		try {
			CompositeConfiguration config = new CompositeConfiguration();
			config.addConfiguration(new PropertiesConfiguration(StringUtils.getRealPath() + "/config/mongodb.properties"));
			// 从配置文件中获取属性值
			String hosts = config.getString("host");
			List<ServerAddress> seeds = new ArrayList<ServerAddress>();
			String[] addressArr = hosts.split(";");
			for(String str : addressArr) {
				String[] hostArr = str.split(":");
				String ip = hostArr[0];
				int port = Integer.valueOf(hostArr[1]);
				ServerAddress seed = new ServerAddress(ip, port);
				seeds.add(seed);
			}
			int poolSize = config.getInt("connectionsPerHost");
			int connectionMultiplier = config.getInt("connectionMultiplier");
			Builder builder = new MongoClientOptions.Builder();
			builder.connectionsPerHost(poolSize);
			builder.threadsAllowedToBlockForConnectionMultiplier(connectionMultiplier);
			builder.maxWaitTime(3000);
			builder.connectTimeout(3000);
			builder.socketKeepAlive(true);
			builder.socketTimeout(3000);// 套接字超时时间，0无限制
			builder.writeConcern(WriteConcern.SAFE);
			log.info("[static] mongoHost=[{}],poolSize=[{}]", hosts, poolSize);
			
			client = new MongoClient(seeds, builder.build());
		} catch (Exception e) {
			log.error("[static] error,Can't connect MongoDB!", e);
		}
	}

	/**
	 * 获取所有数据库名称列表
	 * 
	 * @return
	 */
	public MongoIterable<String> getAllDBNames() {
		MongoIterable<String> s = client.listDatabaseNames();
		return s;
	}

	/**
	 * 删除一个数据库
	 */
	public void dropDB(String dbName) {
		client.getDatabase(dbName).drop();
	}

	/**
	 * 查询DB下的所有表名
	 * 
	 * @param dbName
	 * @return
	 */
	public List<String> getAllCollections(String dbName) {
		MongoDatabase database = client.getDatabase(dbName);
		MongoIterable<String> colls = database.listCollectionNames();
		List<String> _list = new ArrayList<String>();
		for (String s : colls) {
			_list.add(s);
		}
		return _list;
	}

	/**
	 * 获取collection对象
	 * 
	 * @param dbName
	 * @param collName
	 * @return
	 */
	public MongoCollection<Document> getCollection(String dbName, String collName) {
		if (null == collName || "".equals(collName)) {
			return null;
		}
		if (null == dbName || "".equals(dbName)) {
			return null;
		}
//		 DBCollection coll = client.getDB(dbName).getCollection(collName);
//		 return null;
		MongoCollection<Document> collection = client.getDatabase(dbName).getCollection(collName);
		return collection;
	}
	
	/**
	 * 获取collection对象
	 * @param appId
	 * @param collName
	 * @return
	 */
	public MongoCollection<Document> getAppCollection(String appId, String collName) {
		String databaseName = dbName + "_" + appId.trim();
		log.info("[getAppCollection] dbName=[{}]", databaseName);
		MongoCollection<Document> collection = client.getDatabase(databaseName).getCollection(collName);
		return collection;
	}

	/**
	 * 关闭Mongodb
	 */
	public void close() {
		if (client != null) {
			client.close();
			client = null;
		}
	}
	/**
	 * ----------------------------------分割线------------------------------------
	 */

	/**
	 * 插入
	 * 
	 * @param dbName
	 * @param collName
	 * @param doc
	 * @return
	 */
	public boolean insert(String dbName, String collName, Document doc) {
		try {
			MongoCollection<Document> coll = getCollection(dbName, collName);
			coll.insertOne(doc);
		} catch (Exception e) {
			log.error("[insert]:", e);
			return false;
		}
		return true;
	}

	/**
	 * 批量插入
	 * 
	 * @param dbName
	 * @param collName
	 * @param list
	 * @return
	 */
	public boolean insertBatch(String dbName, String collName, List<Document> list) {
		if (list == null || list.isEmpty()) {
			return false;
		}
		try {
			MongoCollection<Document> coll = getCollection(dbName, collName);
			coll.insertMany(list);
		} catch (Exception e) {
			log.error("[nsertBatch]:", e);
			return false;
		}
		return true;
	}

	/**
	 * 删除一个文档
	 * 
	 * @param dbName
	 * @param collName
	 * @param filter
	 * @return
	 */
	public boolean delete(String dbName, String collName, Bson filter) {
		try {
			MongoCollection<Document> coll = getCollection(dbName, collName);
			coll.deleteOne(filter);
		} catch (Exception e) {
			log.error("[delete]:", e);
			return false;
		}
		return true;
	}

	/**
	 * 删除全部
	 * 
	 * @param dbName
	 * @param collName
	 * @param filter
	 * @return
	 */
	public boolean deleteAll(String dbName, String collName, Bson filter) {
		try {
			MongoCollection<Document> coll = getCollection(dbName, collName);
			coll.deleteMany(filter);
		} catch (Exception e) {
			log.error("[deleteAll]:", e);
			return false;
		}

		return true;
	}

	/**
	 * 计算满足条件条数
	 * 
	 * @param dbName
	 * @param collName
	 * @param filter
	 * @return
	 */
	public long getCount(String dbName, String collName, Bson filter) {
		try {
			MongoCollection<Document> coll = getCollection(dbName, collName);
			return coll.count(filter);
		} catch (Exception e) {
			log.error("[getCount]:", e);
		}
		return 0L;
	}

	/**
	 * 查找对象 - 根据主键_id
	 * 
	 * @param dbName
	 * @param collName
	 * @param id
	 * @return
	 */
	public Document findById(String dbName, String collName, String id) {
		ObjectId _idobj = null;
		try {
			_idobj = new ObjectId(id);
			MongoCollection<Document> coll = getCollection(dbName, collName);
			Document myDoc = coll.find(Filters.eq("_id", _idobj)).first();
			return myDoc;
		} catch (Exception e) {
			log.error("[findById]:", e);
		}
		return null;
	}

	/**
	 * 条件查询
	 * 
	 * @param dbName
	 * @param collName
	 * @param filter
	 * @return
	 */
	public MongoCursor<Document> find(String dbName, String collName, Bson filter) {
		MongoCollection<Document> coll = getCollection(dbName, collName);
		return coll.find(filter).iterator();
	}

	/**
	 * 分页查询
	 * 
	 * @param dbName
	 * @param collName
	 * @param filter
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public MongoCursor<Document> findByPage(String dbName, String collName, Bson filter, int pageNo, int pageSize) {
		Bson orderBy = new BasicDBObject("_id", 1);
		MongoCollection<Document> coll = getCollection(dbName, collName);
		return coll.find(filter).sort(orderBy).skip((pageNo - 1) * pageSize).limit(pageSize).iterator();
	}

	/**
	 * 通过ID删除
	 * 
	 * @param dbName
	 * @param collName
	 * @param id
	 * @return
	 */
	public int deleteById(String dbName, String collName, String id) {
		int count = 0;
		try {
			ObjectId _id = new ObjectId(id);
			Bson filter = Filters.eq("_id", _id);
			MongoCollection<Document> coll = getCollection(dbName, collName);
			DeleteResult deleteResult = coll.deleteOne(filter);
			count = (int) deleteResult.getDeletedCount();
		} catch (Exception e) {
			log.error("[deleteById]:", e);
		}
		return count;
	}
	
	/**
     * 修改一个文档
     *
     * @param dbName
     * @param collName
     * @param filter   修改条件
     * @param update   修改内容
     * @return
     */
    public boolean update(String dbName, String collName, Bson filter, Bson update) {
        try {
            MongoCollection<Document> coll = getCollection(dbName, collName);
            // coll.updateOne(filter, update);
            coll.updateOne(filter, new Document("$set", update));
        } catch (Exception e) {
            log.error("BaseDAO update:", e);
            return false;
        }
        return true;
    }

    /**
     * 修改一个文档
     *
     * @param dbName
     * @param collName
     * @param filter        修改条件
     * @param update        修改内容
     * @param updateOptions 更新参数
     * @return
     */
    public boolean update(String dbName, String collName, Bson filter,
                          Bson update, UpdateOptions updateOptions) {
        try {
            MongoCollection<Document> coll = getCollection(dbName, collName);
            coll.updateOne(filter, new Document("$set", update), updateOptions);
        } catch (Exception e) {
            log.error("BaseDAO update:", e);
            return false;
        }
        return true;
    }
	
	public static void main(String[] args) throws Exception {
		//BaseDao manager = new BaseDao();
		//manager.getCollection("db_msg_gomeplus_test_20", "t_group_msg_0");
		for(int i = 22; i < 64; i++) {
			String dbName = "db_msg_gomeplus_test_";
			dbName += i;
			for(int j = 0; j < 2; j++) {
				String tableName = "t_group_msg_";
				tableName += j;
				client.getDatabase(dbName).createCollection(tableName);
			}
		}
//		client.getDatabase("db_msg_gomeplus_test_20").createCollection("t_group_msg_1");
		// DBObject whereFields = new BasicDBObject();
		// whereFields.put("_id", new ObjectId("52870d157003a2c0d6dd38a3"));
		// DBObject setValue=new BasicDBObject();
		// setValue.put("stanza", "liuxm49");
		//
		// DBObject setFields = new BasicDBObject("$set",setValue);
		// manager.update("offline_200", whereFields, setFields);

		// BasicDBObject whereFields = new BasicDBObject();
		// whereFields.put("toUser", "10000005");
		// whereFields.put("fromUser", "10000004");
		// manager.delete("offline_200", whereFields);

		// List<String> colls = manager.listDatabaseNames();
		// for(String collection:colls) {
		// System.out.println(collection);
		// }
		// System.out.println(manager.getCount("offline_200"));

		// manager.closeClient();
	}
}
