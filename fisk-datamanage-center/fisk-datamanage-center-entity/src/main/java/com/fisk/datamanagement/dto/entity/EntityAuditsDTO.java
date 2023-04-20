package com.fisk.datamanagement.dto.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class EntityAuditsDTO {

    @ApiModelProperty(value = "实体ID")
    private String entityId;

    @ApiModelProperty(value = "时间戳")
    private long timestamp;

    @ApiModelProperty(value = "用户")
    private String user;

    @ApiModelProperty(value = "动作")
    private String action;

    @ApiModelProperty(value = "详细信息")
    private String details;

    @ApiModelProperty(value = "关键事件")
    private String eventKey;

    @ApiModelProperty(value = "实体")
    private String entity;

    @ApiModelProperty(value = "类型")
    private String type;
}
