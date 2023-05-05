package com.fisk.datamodel.vo;

import com.fisk.task.enums.DataClassifyEnum;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DataModelVO {

    /**
     * 类型：数据接入/数据建模/mdm
     */
    public DataClassifyEnum dataClassifyEnum;

    /**
     * 业务域id/应用id/模型id
     */
    public String businessId;

    /**
     * 是否删除业务域/应用/模型
     */
    public boolean delBusiness;

    /**
     * 维度表id集合
     */
    public DataModelTableVO dimensionIdList;

    /**
     * 事实表id集合
     */
    public DataModelTableVO factIdList;

    /**
     * 物理表id集合
     */
    public DataModelTableVO physicsIdList;

    /**
     * 指标表id集合
     */
    public DataModelTableVO indicatorIdList;

    /**
     * userid
     */
    public Long userId;

}
