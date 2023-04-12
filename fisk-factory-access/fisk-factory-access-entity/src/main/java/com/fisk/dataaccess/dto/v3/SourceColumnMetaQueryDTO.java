package com.fisk.dataaccess.dto.v3;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class SourceColumnMetaQueryDTO {

    @ApiModelProperty(value = "接入应用id", required = true)
    public long appId;

    @ApiModelProperty(value = "表名或视图名", required = true)
    public String name;

    @ApiModelProperty(value = "查询类型:1表 2视图", required = true)
    public Integer queryType;

}
