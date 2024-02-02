package com.fisk.dataservice.dto.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: wangjian
 * @Date: 2024-01-26
 * @Description:
 */
@Data
public class ApiTreeDTO {

    @ApiModelProperty(value = "")
    private Integer id;

    @ApiModelProperty(value = "父id")
    private Integer pid;

    @ApiModelProperty(value = "标签名称")
    private String name;

    @ApiModelProperty(value = "类型:1:目录 2:数据")
    private Integer type;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "创建时间")
    @JsonIgnore
    public LocalDateTime createTime;

    @ApiModelProperty(value = "子类")
    private List<ApiTreeDTO> children;
}
