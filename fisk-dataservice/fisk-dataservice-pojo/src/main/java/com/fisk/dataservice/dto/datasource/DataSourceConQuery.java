package com.fisk.dataservice.dto.datasource;

import com.fisk.dataservice.enums.DataSourceTypeEnum;

/**
 * @author dick
 * @version v1.0
 * @description 数据源 查询条件
 * @date 2022/1/6 14:51
 */
public class DataSourceConQuery {
    /**
     * 连接名称
     */
    public String name;
    /**
     * 连接类型
     */
    public DataSourceTypeEnum conType;
    /**
     * 账号
     */
    public String conAccount;
    /**
     * 用户Id
     */
    public Long userId;
}
