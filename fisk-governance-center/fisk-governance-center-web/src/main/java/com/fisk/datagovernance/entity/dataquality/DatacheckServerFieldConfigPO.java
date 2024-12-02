package com.fisk.datagovernance.entity.dataquality;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * @author wangjian
 * @date 2024-11-21 14:59:34
 */
@TableName("tb_datacheck_server_field_config")
@Data
public class DatacheckServerFieldConfigPO extends BasePO {

    @ApiModelProperty(value = "API Id")
    private Integer apiId;

    @ApiModelProperty(value = "字段Id")
    private String fieldId;

    @ApiModelProperty(value = "字段名称")
    private String fieldName;

    @ApiModelProperty(value = "字段类型")
    private String fieldType;

    @ApiModelProperty(value = "字段描述")
    private String fieldDesc;

    @ApiModelProperty(value = "返回标识")
    private Integer returnFlag;
}
