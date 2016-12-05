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

import cn.com.gome.logic.model.GroupQuitMember;
import cn.com.gome.logic.utils.BeanTransUtils;

/**
 * 退出群成员数据库操作类
 */
public class GroupQuitMemberDao extends BaseDao {
	Logger log = LoggerFactory.getLogger(GroupQuitMemberDao.class);
	//private final static String dbName = "db_im";
	private final static String collName = "t_group_quit_member";
	
	
	/**
	 * 根据用户id获取
	 * @param uid
	 * @param time 
	 * @return
	 */
	public List<GroupQuitMember> listGroupQuitMember(String appId, long uid, long time) {
		log.info("[listGroupQuitMember] appId=[{}],uid=[{}],time=[{}]", appId, uid, time);
		List<GroupQuitMember> groupMembers = new ArrayList<GroupQuitMember>();
		try {
			MongoCollection<Document> coll = this.getAppCollection(appId, collName);
			Bson where = Filters.and(Filters.eq("uid", uid), Filters.gte("createTime", time));
			//Bson where = Filters.eq("uid", uid);
			MongoCursor<Document> cursor = coll.find(where).iterator();
			while (cursor.hasNext()) {
				Document item = cursor.next();
				GroupQuitMember been = (GroupQuitMember) BeanTransUtils.document2Bean(item, GroupQuitMember.class);
				groupMembers.add(been);
			}
		} catch (Exception e) {
			log.error("listGroupQuitMember:", e);
		}
		
		return groupMembers;
	}
}
