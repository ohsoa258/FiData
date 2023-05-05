package com.fisk.datamanagement.dto.businessmetadata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessMetaDataOptionsDTO {

    @ApiModelProperty(value = "最大字符串长度")
    public String maxStrLength;

    @ApiModelProperty(value = "可应用的实体类型")
    public String applicableEntityTypes;

}
