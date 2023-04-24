package com.fisk.datamodel.dto.businesslimitedattribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class BusinessLimitedAttributeDataDTO {
    @ApiModelProperty(value = "id")
    public int id;
    /**
     * 业务限定id
     */
    @ApiModelProperty(value = "业务限定id")
    public int businessLimitedId;
    /**
     *事实字段表id
     */
    @ApiModelProperty(value = "事实字段表id")
    public int factAttributeId;
    /**
     *计算逻辑
     */
    @ApiModelProperty(value = "计算逻辑")
    public String calculationLogic;
    /**
     *计算值
     */
    @ApiModelProperty(value = "计算值")
    public String calculationValue;
    /**
     * 连接条件
     */
    @ApiModelProperty(value = "连接条件")
    public String joinCondition;
}
