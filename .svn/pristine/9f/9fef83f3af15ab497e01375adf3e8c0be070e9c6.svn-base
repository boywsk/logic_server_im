package cn.com.gome.logic.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;

/**
 * 对象转换工具类
 */
public class BeanTransUtils {
	static Logger log = LoggerFactory.getLogger(BeanTransUtils.class);

	/**
	 * Map --> Bean
	 * 
	 * @param map
	 * @param obj
	 */
	public static void map2Bean(Map<String, Object> map, Object obj) {
		if (map == null || obj == null) {
			return;
		}
		try {
			BeanUtils.populate(obj, map);
		} catch (Exception e) {
			log.error("[transMap2Bean2] Error", e);
		}
	}

	/**
	 * Bean --> Map
	 * @param bean
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> bean2Map(Object bean) {
		if (bean == null) {
			return null;
		}
		String json = JSON.toJSONString(bean);
		Map<String, Object> map = JSON.parseObject(json, Map.class);
		return map;
	}

	/**
	 * Document --> bean
	 * 
	 * @param doc
	 * @param clazz
	 * @return
	 */
	public static <T> Object document2Bean(Document doc, Class<T> clazz) {
		if(null == doc || null == clazz) {
			return null;
		}
		String json = JSON.toJSONString(doc);
		try {
			return JSON.parseObject(json, clazz);
		} catch (Exception e) {
			log.error("", e);
		}

		return null;
	}

	/**
	 * bean --> Document
	 * 
	 * @param bean
	 * @return
	 */
	public static Document bean2Document(Object bean) {
		if(null == bean) {
			return null;
		}
		String json = JSON.toJSONString(bean);
		Document doc = Document.parse(json);

		return doc;
	}

	/**
	 * List<bean> --> List<Document>
	 * 
	 * @param <T>
	 * @param beans
	 * @return
	 */
	public static <T> List<Document> bean2Document2(List<T> beans) {
		if(null == beans) {
			return null;
		}
		List<Document> docs = new ArrayList<Document>();
		for (Object bean : beans) {
			String json = JSON.toJSONString(bean);
			Document doc = Document.parse(json);
			docs.add(doc);
		}

		return docs;
	}
}
