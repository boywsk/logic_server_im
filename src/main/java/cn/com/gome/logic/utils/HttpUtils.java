package cn.com.gome.logic.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * http 工具类
 */
public class HttpUtils {
	static Logger log = LoggerFactory.getLogger(HttpUtils.class);

	/**
	 * http post
	 * 
	 * @param addressHost
	 * @param parameters
	 * @param protocal
	 * @param jsonOrxml
	 * @return
	 * @throws java.io.IOException
	 */
	public static String sendRequest(String addressHost, Map<String, String> parameters, String protocal,
			String jsonOrxml) throws IOException {
		HttpURLConnection urlConnection = null;
		if (parameters != null) {
			StringBuffer param = new StringBuffer();
			int i = 0;
			for (String key : parameters.keySet()) {
				if (i == 0)
					param.append("?");
				else
					param.append("&");
				param.append(key).append("=").append(parameters.get(key));
				i++;
			}
			addressHost += param;
		}
		URL url = new URL(addressHost);
		urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestProperty("Content-Type", protocal);
		urlConnection.setRequestProperty("Accept-Charset", "utf-8");
		urlConnection.setRequestProperty("Accept", protocal);
		urlConnection.setRequestMethod("POST");
		urlConnection.setRequestProperty("Connection", "close");
		urlConnection.setConnectTimeout(3000);
		urlConnection.setReadTimeout(3000);
		urlConnection.setDoOutput(true);
		urlConnection.setDoInput(true);
		urlConnection.setUseCaches(false);

		OutputStream output = null;
		try {
			output = urlConnection.getOutputStream();
			output.write(jsonOrxml.toString().getBytes("UTF-8"));
		} catch (Exception e) {
			log.error("sendRequest:", e);
		} finally {
			if (output != null) {
				output.close();
			}
		}

		return makeContent(addressHost, urlConnection);
	}

	/**
	 * 得到响应对象
	 * 
	 * @paramurlConnection
	 * @return响应对象
	 * @throwsIOException
	 */
	private static String makeContent(String urlString, HttpURLConnection urlConnection) throws IOException {
		StringBuffer temp = new StringBuffer();
		try {
			InputStream in = urlConnection.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line = bufferedReader.readLine();
			while (line != null) {

				temp.append(line);
				line = bufferedReader.readLine();
			}

			bufferedReader.close();

			String ecod = urlConnection.getContentEncoding();
			if (ecod == null)
				ecod = "utf-8";

		} catch (IOException e) {
			log.error("makeContent:", e);
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}
		return temp.toString();
	}

	/**
	 * 向指定URL发送GET方法的请求
	 * 
	 * @param url
	 *            发送请求的URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return URL 所代表远程资源的响应结果
	 */
	public static String sendGet(String url, String param) {
		String result = "";
		BufferedReader in = null;
		try {
			if (param != null && param.length() > 0) {
				url = url + "?" + param;
			}

			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立实际的连接
			connection.connect();
			// // 获取所有响应头字段
			// Map<String, List<String>> map = connection.getHeaderFields();
			// // 遍历所有的响应头字段
			// for (String key : map.keySet()) {
			// System.out.println(key + "--->" + map.get(key));
			// }
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			log.error("sendGet:", e);
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}

}
