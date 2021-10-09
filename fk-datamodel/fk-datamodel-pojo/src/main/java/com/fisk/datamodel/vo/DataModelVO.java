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
     * 类型：数据接入/数据建模
     */
    public DataClassifyEnum dataClassifyEnum;

    /**
     * 业务域id
     */
    public long businessId;

    /**
     * 是否删除业务域
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


}
