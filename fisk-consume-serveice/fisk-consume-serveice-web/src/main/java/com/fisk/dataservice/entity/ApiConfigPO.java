package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version v1.0
 * @description api实体类
 * @date 2022/1/6 14:51
 */
@Data
@TableName("tb_api_config")
public class ApiConfigPO extends BasePO
{
    /**
     * api名称
     */
    public String apiName;

    /**
     * api标识code
     */
    public String apiCode;

    /**
     * api描述
     */
    public String apiDesc;

    /**
     * api类型 1 sql、2 自定义sql
     */
    public int apiType;

    /**
     * 数据源id
     */
    public int datasourceId;

    /**
     * 表名称
     */
    public String tableName;

    /**
     * 表别名
     */
    public String tableNameAlias;

    /**
     * 表类型 1：表  2：视图
     */
    public int tableType;

    /**
     * 表业务类型 1：事实表、2：维度表、3、指标表  4、宽表
     */
    public int tableBusinessType;

    /**
     * 表路径
     */
    public String tablePath;

    /**
     * sql语句
     */
    public String createSql;

    /**
     * api地址
     */
    public String apiAddress;

    /**
     * 创建api类型：1 创建新api 2 使用现有api
     */
    public Integer createApiType;
}
