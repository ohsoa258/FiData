package com.fisk.datamodel.dto.dimension;

import com.fisk.datamodel.dto.dimensionattribute.ModelAttributeMetaDataDTO;
import lombok.Data;

import java.util.List;

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
     * 维度表table简称前缀名称
     */
    public String appbAbreviation;
    /**
     * 维度字段列表
     */
    public List<ModelAttributeMetaDataDTO> dto;
    //处理后的表字段
    public List<String> fieldEnNames;
    //存放存储过程名称
    public String sqlName;

}
