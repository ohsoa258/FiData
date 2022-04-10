package com.fisk.mdm.vo.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class ModelVO {


    @ApiModelProperty(value = "主键")
    public int id;

    @ApiModelProperty(value = "model名称")
    public String name;

    @ApiModelProperty(value = "model展示名称")
    public String displayName;

    @ApiModelProperty(value = "model描述")
    public String desc;

    @ApiModelProperty(value = "model日志保存天数")
    public int logRetentionDays;

    @ApiModelProperty(value = "logo保存地址")
    public String logoPath;

    @ApiModelProperty(value = "创建人")
    public String createUser;

    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    @ApiModelProperty(value = "更新人")
    public String updateUser;

    @ApiModelProperty(value = "更新时间")
    public LocalDateTime updateTime;
}
