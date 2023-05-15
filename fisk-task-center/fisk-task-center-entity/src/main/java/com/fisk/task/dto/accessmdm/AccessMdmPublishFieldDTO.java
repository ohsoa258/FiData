package com.fisk.task.dto.accessmdm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wangjian
 */
@Data
public class AccessMdmPublishFieldDTO {
    /**
     * 实体字段id
     */
    @ApiModelProperty(value = "实体字段id")
    public long fieldId;

    /**
     * 实体字段名称
     */
    @ApiModelProperty(value = "实体字段名称")
    public String fieldName;
    /**
     * 是否业务主键 0:否 1:是
     */
    @ApiModelProperty(value = "是否业务主键 0:否 1:是")
    public int isPrimaryKey;
    /**
     * 源字段名称
     */
    @ApiModelProperty(value = "源字段名称")
    public String sourceFieldName;
    /**
     * 实体id
     */
    @ApiModelProperty(value = "实体id")
    public int entityId;
}
