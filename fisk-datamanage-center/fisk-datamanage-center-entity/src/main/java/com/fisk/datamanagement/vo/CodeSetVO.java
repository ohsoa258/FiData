package com.fisk.datamanagement.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: wangjian
 * @Date: 2024-01-30
 * @Description:
 */
@Data
public class CodeSetVO {
    @ApiModelProperty(value = "id")
    public Integer id;
    @ApiModelProperty(value = "编号")
    public String code;
    @ApiModelProperty(value = "名称")
    public String name;
    @ApiModelProperty(value = "描述")
    public String description;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public LocalDateTime createTime;

    @ApiModelProperty(value = "创建人")
    public String createUser;
}
