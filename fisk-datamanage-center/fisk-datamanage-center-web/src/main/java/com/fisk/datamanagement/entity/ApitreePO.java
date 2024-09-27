package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author wangjian
 * @date 2024-09-24 14:18:07
 */
@TableName("tb_apitreelist")
@Data
public class ApitreePO extends BasePO {

    @ApiModelProperty(value = "pid")
    private String pid;
    @ApiModelProperty(value = "appId")
    private String appId;
    @ApiModelProperty(value = "apiId")
    private String apiId;
    @ApiModelProperty(value = "属性Id")
    private String attributeId;
}
