package com.fisk.dataaccess.dto.datatargetapp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author JianWenYang
 */
@Data
public class DataTargetAppDTO {

    @ApiModelProperty(value = "主键,新增时默认不填")
    public Long id;

    @ApiModelProperty(value = "数据目标应用名称", required = true)
    public String name;

    @ApiModelProperty(value = "描述")
    public String description;

    @ApiModelProperty(value = "负责人", required = true)
    public String principal;

    @ApiModelProperty(value = "负责人邮箱", required = true)
    public String email;

    @ApiModelProperty(value = "创建时间,新增时默认不填")
    public LocalDateTime createTime;

}
