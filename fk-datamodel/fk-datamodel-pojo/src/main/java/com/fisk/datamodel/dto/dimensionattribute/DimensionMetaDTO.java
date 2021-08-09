package com.fisk.datamodel.dto.dimensionattribute;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionMetaDTO {
    /**
     * 关联维度id
     */
    public long associateDimensionId;
    /**
     * 维度表名
     */
    public String tableName;
    public List<DimensionAttributeAssociationDTO> field;
}
