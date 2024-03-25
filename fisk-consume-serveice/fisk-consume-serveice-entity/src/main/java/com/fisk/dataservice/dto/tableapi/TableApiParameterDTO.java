package com.fisk.dataservice.dto.tableapi;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: wangjian
 * @Date: 2023-09-12
 * @Description:
 */
@Data
public class TableApiParameterDTO {
    @ApiModelProperty(value = "id")
    private int id;

    @ApiModelProperty(value = "pid")
    private int pid;

    @ApiModelProperty(value = "id",hidden = true)
    private int copyId;

    @ApiModelProperty(value = "pid",hidden = true)
    private int copyPid;

    @ApiModelProperty(value = "apiId")
    private Integer apiId;

    @ApiModelProperty(value = "字段类型")
    private Integer parameterType;

    @ApiModelProperty(value = "字段名称")
    private String parameterName;

    @ApiModelProperty(value = "字段值")
    private String parameterValue;

    @ApiModelProperty(value = "1选中2不选")
    private String selected;

    @ApiModelProperty(value = "是否加密")
    private int encrypt;

    @ApiModelProperty(value = "是否作用于密钥key")
    private int encryptKey;
}
