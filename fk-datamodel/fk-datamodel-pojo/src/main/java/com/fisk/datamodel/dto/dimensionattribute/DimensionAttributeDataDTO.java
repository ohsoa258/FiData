package com.fisk.datamodel.dto.dimensionattribute;

import lombok.Data;
import org.omg.CORBA.PUBLIC_MEMBER;

/**
 * @author JianWenYang
 */
@Data
public class DimensionAttributeDataDTO {

    public long id;
    /**
     * 维度字段中文名
     */
    public String dimensionFieldCnName;
    /**
     * 维度字段英文名
     */
    public String dimensionFieldEnName;
    /**
     * 维度字段描述
     */
    public String dimensionFieldDes;
    /**
     * 关联维度表id
     */
    public int associateDimensionId;
    /**
     * 关联维度表名称
     */
    public String associateDimensionName;
    /**
     * 关联维度表字段id
     */
    public int associateDimensionFieldId;
    /**
     * 关联维度表字段名称
     */
    public String associateDimensionFieldName;

}
