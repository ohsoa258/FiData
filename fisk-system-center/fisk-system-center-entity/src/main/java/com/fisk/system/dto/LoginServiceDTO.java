package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class LoginServiceDTO {
    /**
     * ID
     */
    @ApiModelProperty(value = "id")
    public Long id;

    /**
     *服务中文名称
     */
    @ApiModelProperty(value = "服务中文名称")
    public String name;

    /**
     * 服务url
     */
    @ApiModelProperty(value = "服务url")
    public String path;

    @ApiModelProperty(value = "组件")
    public String component;

    @ApiModelProperty(value = "源标签")
    public IconDTO meta;

    /**
     * 是否有权限
     */
    @ApiModelProperty(value = "是否有权限")
    public Boolean authority = false;

    @ApiModelProperty(value = "服务代码")
    public String serveCode;

    @ApiModelProperty(value = "描述")
    public String description;
    @ApiModelProperty(value = "无序列")
    public Integer sequenceNo;

    @ApiModelProperty(value = "子类")
    public List<LoginServiceDTO> children;
}
