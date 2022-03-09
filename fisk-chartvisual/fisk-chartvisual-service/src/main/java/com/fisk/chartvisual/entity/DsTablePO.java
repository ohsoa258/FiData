package com.fisk.chartvisual.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/3/4 10:51
 */
@TableName("tb_ds_table")
@Data
public class DsTablePO extends BasePO {

    /**
     * 表名
     */
    private String tableName;
    /**
     * 数据源id
     */
    private Integer dataSourceId;
}
