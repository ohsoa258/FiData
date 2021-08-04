package com.fisk.datamodel.dto.dimension;

import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeMetaDataDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionMetaDataDTO {
    /**
     * 维度表id
     */
    public long id;
    /**
     * 维度表table名称
     */
    public String dimensionTabName;
    /**
     * 维度字段列表
     */
    public List<DimensionAttributeMetaDataDTO> dto;

}
