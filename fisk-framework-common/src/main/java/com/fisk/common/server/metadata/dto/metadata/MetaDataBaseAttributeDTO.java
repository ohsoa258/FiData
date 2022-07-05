package com.fisk.common.server.metadata.dto.metadata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 * @date 2022-07-01 14:26
 */
@Data
public class MetaDataBaseAttributeDTO {
    @ApiModelProperty(value = "限定名")
    public String qualifiedName;
    @ApiModelProperty(value = "名称")
    public String name;
    @ApiModelProperty(value = "联系人信息")
    public String contact_info;
    @ApiModelProperty(value = "描述")
    public String description;
    @ApiModelProperty(value = "说明")
    public String comment;
}
