package com.fisk.datamodel.dto.dimensionattribute;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DimensionMetaDTO {
    public String tableName;
    public List<String> field;
}
