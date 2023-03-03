package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 表应用数据源
 * @date 2023/3/3 11:45
 */
@Data
@TableName("tb_table_app_datasource")
public class TableAppDatasourcePO extends BasePO {
    /**
     * 表应用id
     */
    public int tableAppId;

    /**
     * 数据源类型 1源数据库数据源 2目标数据库数据源
     */
    public int datasourceType;

    /**
     * FiData数据源id
     */
    public int datasourceId;
}
