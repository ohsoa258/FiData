package com.fisk.dataservice.vo.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.dataservice.enums.ApiTypeEnum;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

/**
 * @author dick
 * @version v1.0
 * @description api列表 VO
 * @date 2022/1/10 17:51
 */
public class ApiRegisterVO {
    /**
     * Id
     */
    @ApiModelProperty(value = "主键")
    public int id;

    /**
     * api名称
     */
    @ApiModelProperty(value = "api名称")
    public String apiName;

    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * api标识code
     */
    @ApiModelProperty(value = "api标识code")
    public String apiCode;

    /**
     * api描述
     */
    @ApiModelProperty(value = "api描述")
    public String apiDesc;
}
