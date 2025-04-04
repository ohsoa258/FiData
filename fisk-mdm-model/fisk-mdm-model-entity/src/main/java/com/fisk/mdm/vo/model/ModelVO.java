package com.fisk.mdm.vo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fisk.common.core.baseObject.vo.BaseUserInfoVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


import java.time.LocalDateTime;


/**
 * @author ChenYa
 * @date 2022/4/14 20:35
 */
@Data
public class ModelVO extends BaseUserInfoVO {


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

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime updateTime;
}
