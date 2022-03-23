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
public class EntityIdAndTypeDTO {
    @ApiModelProperty(value = "实例/数据库/表/字段 guid")
    public String guid;
    @ApiModelProperty(value = "实体类型:rdbms_instance、rdbms_db、rdbms_table、rdbms_column")
    public String typeName;
}
