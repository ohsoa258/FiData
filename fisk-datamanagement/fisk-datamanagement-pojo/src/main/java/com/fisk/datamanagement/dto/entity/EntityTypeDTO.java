package com.fisk.datamanagement.dto.entity;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class EntityTypeDTO {

    /**
     * 实体类型:rdbms_instance、rdbms_db、rdbms_table、rdbms_column
     */
    public String typeName;

    public EntityAttributesDTO attributes;

}
