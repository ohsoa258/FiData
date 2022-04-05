package com.fisk.mdm.dto.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class ModelDTO {

    @ApiModelProperty(value = "modelID")
    @NotNull()
    public int id;

    @ApiModelProperty(value = "model名称")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String name;

    @ApiModelProperty(value = "model展示名称")
    @NotNull()
    @Length(min = 0, max = 50, message = "长度最多50")
    public String displayName;

    @ApiModelProperty(value = "model描述")
    @NotNull()
    @Length(min = 0, max = 200, message = "长度最多200")
    public String desc;

    @ApiModelProperty(value = "model日志保留天数")
    @NotNull()
    public int logRetentionDays;

    @ApiModelProperty(value = "logo保存地址")
    @NotNull()
    @Length(min = 0, max = 100, message = "长度最多100")
    public String logoPath;
}
