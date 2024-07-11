package com.fisk.datamanagement.dto.metadataglossarymap;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-03 17:23
 * @description
 */
@Data
public class MetadataEntitySimpleDTO implements Serializable {

    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "id")
    public String entityName;

    @ApiModelProperty(value = "元数据实体限定名称")
    public String metadataQualifiedName;

    @ApiModelProperty(value = "类型")
    public Integer type;

}
