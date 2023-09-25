package com.fisk.dataservice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author wangjian
 * @date 2023-09-08 15:07:20
 */
@Data
@TableName("tb_table_api_auth_request")
public class TableApiAuthRequestPO extends BasePO {

    @ApiModelProperty(value = "app_id")
    private Integer appId;

    @ApiModelProperty(value = "父id")
    private Integer pid;

    @ApiModelProperty(value = "form-data   or   raw ")
    private String requestMethod;

    @ApiModelProperty(value = "请求参数key")
    private String parameterKey;

    @ApiModelProperty(value = "请求参数value")
    private String parameterValue;

}
