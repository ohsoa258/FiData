package com.fisk.datamodel.vo;

import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DataIndicatorVO {

    /**
     * 类型：数据接入/数据建模
     */
    public DataClassifyEnum dataClassifyEnum;

    /**
     * 业务域id
     */
    public long businessId;

    /**
     * 指标表名称
     */
    public String indicatorTable;

    /**
     * 指标名称
     */
    public String indicatorName;

    /**
     * 是否删除指标表
     */
    public boolean delIndicatorTable;

    /**
     * 表类型：维度/事实..
     */
    public OlapTableEnum type;

}
