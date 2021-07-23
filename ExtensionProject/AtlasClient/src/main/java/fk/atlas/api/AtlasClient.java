//author:Dennyhui
//createTime:2021年6月29日12:45:15
//description:Atlas for java skd client
package fk.atlas.api;

import com.alibaba.fastjson.JSON;
import fk.atlas.api.model.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class AtlasClient {
    public String BaseUrl;
    public String User;
    public String Pwd;
    public String ApiUrl;
    public String EntityActionUrl = "/api/atlas/v2/entity";
    public String ProcessActionUrl = "/api/atlas/v2/entity/bulk";
    public String DeleteEntityActionUrl = "/api/atlas/v2/entity/guid/";
    public String SearchEntityActionUrl="/api/atlas/v2/search/basic";

    /**
     * @param _baseUrl 服务器地址
     * @param _user    用户名
     * @param _pwd     密码
     */
    public AtlasClient(String _baseUrl, String _user, String _pwd) {
        BaseUrl = _baseUrl;
        User = _user;
        Pwd = _pwd;
        ApiUrl = BaseUrl + EntityActionUrl;
    }

    /**
     * 创建数据源实例
     *
     * @param _model_rdbms_instance
     * @return
     */
    public String CreateEntity_rdbms_instance(EnttityRdbmsInstance.entity_rdbms_instance _model_rdbms_instance) {
        String result = null;
        String paramJson = JSON.toJSONString(_model_rdbms_instance);
        try {
            result = post(ApiUrl, paramJson);
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    /**
     * 创建数据库
     *
     * @param _model_rdbms_db
     * @return
     */
    public String CreateEntity_rdbms_db(EntityRdbmsDB.entity_rdbms_db _model_rdbms_db) {
        String result = null;
        String paramJson = JSON.toJSONString(_model_rdbms_db);
        try {
            result = post(ApiUrl, paramJson);
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    /**
     * 创建表
     *
     * @param _model_rdbms_table
     * @return
     */
    public String CreateEntity_rdbms_table(EntityRdbmsTable.entity_rdbms_table _model_rdbms_table) {
        String result = null;
        String paramJson = JSON.toJSONString(_model_rdbms_table);
        try {
            result = post(ApiUrl, paramJson);
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    /**
     * 创建表字段
     *
     * @param _model_rdbms_table_column
     * @return
     */
    public String CreateEntity_rdbms_table_column(EntityRdbmsColumn.entity_rdbms_column _model_rdbms_table_column) {
        String result = null;
        String paramJson = JSON.toJSONString(_model_rdbms_table_column);
        try {
            result = post(ApiUrl, paramJson);
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    /**
     * 创建实体连接
     *
     * @param entity_rdbms_process
     * @return
     */
    public String CreateEntityProcess(EntityProcess.entity_rdbms_process entity_rdbms_process) {
        String result = null;
        String paramJson = JSON.toJSONString(entity_rdbms_process);
        try {
            result = post(this.BaseUrl + ProcessActionUrl, paramJson);
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    /**
     * 删除实体
     *
     * @param EntityGUID
     * @return
     */
    public String DeleteEntity(String EntityGUID) {
        String result = null;
        try {
            result = delete(this.BaseUrl + DeleteEntityActionUrl + EntityGUID);
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    public String GetEntity(String entityGUID) {
        String result = null;
        try {
            result = post(this.BaseUrl + DeleteEntityActionUrl, entityGUID);
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    /**
     * 获取Atlas数据
     * @return
     */
    public String GetEntity() {
        String result = null;
        try {
            result = sendGet(this.BaseUrl + SearchEntityActionUrl+"?typeName=rdbms_instance");
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    /**
     * 发送请求HTTP-POST请求 url:请求地址; entity:json格式请求参数
     *
     * @param url
     * @param entity
     * @return
     */
    public String post(String url, String entity) {
        String resStr = null;
        try {
            // 账户
            String username = User;
            // 密码
            String password = Pwd;
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Authorization", "Basic " + java.util.Base64.getUrlEncoder().encodeToString((username + ":" + password).getBytes()));
            StringEntity se = new StringEntity(entity, "UTF-8");
            se.setContentType("application/json");
            httpPost.setEntity(se);
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity1 = response.getEntity();
            if (entity1 != null) {
                resStr = EntityUtils.toString(entity1, "UTF-8");
            }
            httpClient.close();
            response.close();
        } catch (Exception e) {
            resStr = e.getMessage();
        }
        return resStr;
    }

    public static void doDelete(String urlToRead) throws Exception {
        URL url = new URL(urlToRead);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setDoOutput(true);
        httpCon.setRequestProperty(
                "Content-Type", "application/x-www-form-urlencoded");
        httpCon.setRequestMethod("DELETE");
        httpCon.connect();
        httpCon.disconnect();

    }

    /**
     * 发送请求HTTP-DELETE请求 url:请求地址;
     *
     * @param url
     * @return
     */
    public String delete(String url) {
        String resStr = null;
        // 账户
        String username = User;
        // 密码
        String password = Pwd;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.addHeader("Authorization", "Basic " + java.util.Base64.getUrlEncoder().encodeToString((username + ":" + password).getBytes()));
        httpDelete.setHeader("Content-type", "application/json");
        httpDelete.setHeader("DataEncoding", "UTF-8");

        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpDelete);
            HttpEntity entity = httpResponse.getEntity();
            resStr = EntityUtils.toString(entity);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            resStr = e.getMessage();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            resStr = e.getMessage();
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    resStr = e.getMessage();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    resStr = e.getMessage();
                }
            }
        }
        return resStr;
    }

    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url   发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            // 账户
            String username = User;
            // 密码
            String password = Pwd;
            String urlNameString = url + (param == null ? "" : "?" + param);
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.addRequestProperty("Authorization", "Basic " + java.util.Base64.getUrlEncoder().encodeToString((username + ":" + password).getBytes()));
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
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

    public  String sendGet(String urlParam) {
        String result = "";
        // 账户
        String username = User;
        // 密码
        String password = Pwd;
        try {
            // 创建httpClient实例对象
            HttpClient httpClient = new HttpClient();
            // 设置httpClient连接主机服务器超时时间：15000毫秒
            httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(15000);
            // 创建GET请求方法实例对象
            GetMethod getMethod = new GetMethod(urlParam);
            // 设置post请求超时时间
            getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 60000);
            getMethod.addRequestHeader("Content-Type", "application/json");
            getMethod.addRequestHeader("Authorization", "Basic " + java.util.Base64.getUrlEncoder().encodeToString((username + ":" + password).getBytes()));
            httpClient.executeMethod(getMethod);
            result = getMethod.getResponseBodyAsString();
            getMethod.releaseConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}

