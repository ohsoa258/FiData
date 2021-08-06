package com.fisk.datamodel.dto.factattribute;

/**
 * @author JianWenYang
 */
public class FactAttributeListDTO extends FactAttributeDTO {
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
