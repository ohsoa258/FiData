package com.fisk.datamanagement.dto.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author JianWenYang
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EntityTypeDTO {

    @ApiModelProperty(value = "实体类型:rdbms_instance、rdbms_db、rdbms_table、rdbms_column", required = true)
    /**
     * 实体类型:rdbms_instance、rdbms_db、rdbms_table、rdbms_column
     */
    public String typeName;

    @ApiModelProperty(value = "属性")
    public EntityAttributesDTO attributes;

}
