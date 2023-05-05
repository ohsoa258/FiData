package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ServiceRegistryDTO {

    @ApiModelProperty(value = "Id")
    public  int id;

    /**
     *服务code
    */
    @ApiModelProperty(value = "服务code")
    public  String serveCode;

    /**
    *上一级服务code
    */
    @ApiModelProperty(value = "上一级服务code")
    public  String parentServeCode;

    /**
    *服务中文名称
    */
    @ApiModelProperty(value = "服务中文名称")
    public  String serveCnName;

    /**
    *服务英文名称
    */
    @ApiModelProperty(value = "服务英文名称")
    public  String serveEnName;

    /**
    *服务url
    */
    @ApiModelProperty(value = "服务url")
    public  String serveUrl;

    /**
     * 服务图标
     */
    @ApiModelProperty(value = "服务图标")
    public String icon;

    /**
     * 排序号
     */
    @ApiModelProperty(value = "排序号")
    public int sequenceNo;

    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    public String description;

    /**
     * 父级服务下一级服务list
     */
    @ApiModelProperty(value = "父级服务下一级服务list")
    public List<ServiceRegistryDTO> dtos;

}
