package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
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
     * sql语句
     */
    public String createSql;
}
