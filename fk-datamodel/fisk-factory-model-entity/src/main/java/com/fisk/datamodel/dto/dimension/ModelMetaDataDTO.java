package com.fisk.datamodel.dto.dimension;

import com.fisk.datamodel.dto.dimensionattribute.ModelAttributeMetaDataDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author JianWenYang
 */
@Data
public class ModelMetaDataDTO {
    /**
     * 维度表id
     */
    public long id;
    /**
     * 维度表table名称
     */
    public String tableName;

    /**
     * 数据接入app_id、数据建模business_id
     */
    public int appId;
    /**
     * 维度表table简称前缀名称
     */
    public String appbAbreviation;
    /**
     * 维度来源表名称(ODS)
     */
    public String sourceTableName;
    /**
     * 维度字段列表
     */
    public List<ModelAttributeMetaDataDTO> dto;
    //处理后的表字段
    public List<String> fieldEnNames;
    //存放存储过程名称
    public String sqlName;

    public Map<String, String> fieldEnNameMaps;

    /*
    * 组id,仅管道用的到
    * */
    public String groupComponentId;

}
