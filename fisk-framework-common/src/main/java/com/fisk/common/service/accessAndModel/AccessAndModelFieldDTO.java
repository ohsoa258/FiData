package com.fisk.common.service.accessAndModel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AccessAndModelFieldDTO {

    @ApiModelProperty(value = "id")
    private long id;

    /**
     * 英文字段名称
     */
    @ApiModelProperty(value = "英文字段名称")
    public String FieldEnName;

    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型")
    public String fieldType;

}
