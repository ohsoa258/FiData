package com.fisk.datamanagement.dto.datamasking;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 * @description 数据脱敏出参dto
 * @date 2022/4/15 14:00
 */
@Data
public class DataMaskingTargetDTO {

    @ApiModelProperty(value = "地址")
    public String url;

    @ApiModelProperty(value = "用户名")
    public String username;

    @ApiModelProperty(value = "密码")
    public String password;

    @ApiModelProperty(value = "表名")
    public String tableName;
}
