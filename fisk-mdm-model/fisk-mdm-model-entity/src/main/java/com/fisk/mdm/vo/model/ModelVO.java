package com.fisk.mdm.vo.model;

import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;

public class ModelVO {


    @ApiModelProperty(value = "modelID")
    public int id;

    @ApiModelProperty(value = "model名称")
    public String name;

    @ApiModelProperty(value = "model展示名称")
    public String displayName;

    @ApiModelProperty(value = "model描述")
    public String desc;

    @ApiModelProperty(value = "model日志保存天数")
    public int logRetentionDays;

    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    @ApiModelProperty(value = "更新人")
    public String updateUser;

    @ApiModelProperty(value = "更新时间")
    public LocalDateTime updateTime;
}
