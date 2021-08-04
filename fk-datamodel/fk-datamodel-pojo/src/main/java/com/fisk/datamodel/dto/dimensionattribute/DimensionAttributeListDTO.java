package com.fisk.datamodel.dto.dimensionattribute;

import com.fisk.datamodel.dto.dimensionattribute.DimensionAttributeDTO;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class DimensionAttributeListDTO extends DimensionAttributeDTO {
    /**
     * 主键
     */
    public int id;
    /**
     * 关联维度表名称
     */
    public String dimensionCnName;
    /**
     * 源表名
     */
    public String tableSource;
    /**
     * 源表字段
     */
    public String tableSourceField;

}
