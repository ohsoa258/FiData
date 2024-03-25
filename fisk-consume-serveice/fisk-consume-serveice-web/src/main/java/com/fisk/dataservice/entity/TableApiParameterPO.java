package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;
/**
 * @author wangjian
 * @date 2023-09-08 15:44:39
 */
@Data
@TableName("tb_table_api_parameter")
public class TableApiParameterPO extends BasePO {

    @ApiModelProperty(value = "api_id")
    private Integer apiId;

    @ApiModelProperty(value = "字段类型")
    private Integer parameterType;

    @ApiModelProperty(value = "字段名称")
    private String parameterName;

    @ApiModelProperty(value = "pid")
    private int pid;

    @ApiModelProperty(value = "字段值")
    private String parameterValue;

    @ApiModelProperty(value = "1选中2不选")
    private Integer selected;

    @ApiModelProperty(value = "是否加密")
    private int encrypt;

    @ApiModelProperty(value = "是否作用于密钥key")
    private int encryptKey;
}
