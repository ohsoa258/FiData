package com.fisk.datagovernance.dto.dataquality.datacheck.apidoc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description API返回参数
 * @date 2022/2/3 14:10
 */
@Data
public class ApiResponseDTO {
    /**
     * 表名
     */
    @ApiModelProperty(value = "表名")
    public String tableName;
    /**
     * 参数名称
     */
    @ApiModelProperty(value = "参数名称")
    public String  parmName;

    /**
     * 参数类型
     */
    @ApiModelProperty(value = "参数类型")
    public String  parmType;

    /**
     * 参数描述
     */
    @ApiModelProperty(value = "参数描述")
    public String  parmDesc;

    /**
     * 参数是否加密
     */
    @ApiModelProperty(value = "校验结果回显标识")
    public String  parmReturnFlag;

    /**
     * 行样式
     */
    @ApiModelProperty(value = "行样式")
    public String  trStyle;
}
