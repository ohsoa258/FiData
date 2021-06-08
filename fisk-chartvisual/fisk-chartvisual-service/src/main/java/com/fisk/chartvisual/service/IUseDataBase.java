package com.fisk.chartvisual.service;

import com.fisk.chartvisual.dto.ChartQueryObject;
import com.fisk.chartvisual.vo.DataServiceVO;
import com.fisk.common.enums.chartvisual.DataSourceTypeEnum;

import java.util.List;
import java.util.Map;

/**
 * 使用数据库
 *
 * @author gy
 */
public interface IUseDataBase {

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
    List<Map<String, Object>> query(ChartQueryObject query);
}
