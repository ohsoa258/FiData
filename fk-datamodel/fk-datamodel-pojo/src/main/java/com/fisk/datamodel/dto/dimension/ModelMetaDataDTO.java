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
     * 维度字段列表
     */
    public List<ModelAttributeMetaDataDTO> dto;

    public List<String> fieldEnNames;

}
