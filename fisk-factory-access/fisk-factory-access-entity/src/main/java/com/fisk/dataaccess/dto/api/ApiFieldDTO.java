package com.fisk.dataaccess.dto.api;

import io.swagger.annotations.ApiModelProperty;

public class ApiFieldDTO {

    @ApiModelProperty(value = "id", required = true)
    public long id;

    @ApiModelProperty(value = "appId", required = true)
    public long appId;

    @ApiModelProperty(value = "tableAccessId", required = true)
    public long tableAccessId;

    @ApiModelProperty(value = "字段ID", required = true)
    public long fieldId;

    @ApiModelProperty(value = "字段名称", required = true)
    public String fieldName;

    @ApiModelProperty(value = "字段描述", required = true)
    public String fieldDesc;

    @ApiModelProperty(value = "显示名称", required = true)
    public String displayName;

    @ApiModelProperty(value = "字段类型", required = true)
    public String fieldType;

    @ApiModelProperty(value = "字段长度", required = true)
    public long fieldLength;

    @ApiModelProperty(value = "实时api的json结构示例")
    public String pushData;
    @ApiModelProperty(value = "true: 勾选(发布之后,按照配置调用一次api);false: 不勾选")
    public Boolean executeConfigFlag;
}
