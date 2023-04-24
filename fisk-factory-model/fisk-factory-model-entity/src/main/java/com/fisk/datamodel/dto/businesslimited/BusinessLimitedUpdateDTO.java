package com.fisk.datamodel.dto.businesslimited;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessLimitedUpdateDTO {
    @ApiModelProperty(value = "id")
    public long id;
    /**
     * 业务限定名称
     */
    @ApiModelProperty(value = "业务限定名称")
    public String limitedName;
    /**
     * 业务限定描述
     */
    @ApiModelProperty(value = "业务限定描述")
    public String limitedDes;
}
