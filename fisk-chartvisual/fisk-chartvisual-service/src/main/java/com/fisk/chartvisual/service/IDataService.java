package com.fisk.chartvisual.service;

import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.dto.ChartQueryObjectSsas;
import com.fisk.chartvisual.dto.SlicerQueryObject;
import com.fisk.chartvisual.vo.DataServiceResult;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 数据服务
 * @author gy
 */
public interface IDataService {

    /**
     * 测试数据库连接
     * @param type 数据源类型
     * @param con 连接字符串
     * @param acc 账号
     * @param pwd 密码
     * @return 是否成功连接
     */
    boolean testConnection(DataSourceTypeEnum type, String con, String acc, String pwd);

    /**
     * 根据表/字段信息，查询数据
     * @param query 表/字段信息
     * @return 查询结果
     */
    DataServiceResult query(ChartQueryObject query);

    /**
     * 下载数据
     * @param key redis key
     * @param response http请求响应
     */
    void downLoad(String key, HttpServletResponse response);

    /**
     * 获取切片器的数据
     * @param query 表/字段信息
     * @return 查询结果
     */
    List<Map<String, Object>> querySlicer(SlicerQueryObject query);

    /**
     * 根据维度度量，查询数据
     * @param query 表/字段信息
     * @return 查询结果
     */
    DataServiceResult querySsas(ChartQueryObjectSsas query);
}
