package com.fisk.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ServiceSourceDTO {

    /**
     *服务中文名称
     */
    @ApiModelProperty(value = "服务中文名称")
    public String serveCnName;

    /**
     *服务url
     */
    @ApiModelProperty(value = "服务url")
    public  String serveUrl;

    /**
     *服务图标
     */
    @ApiModelProperty(value = "服务图标")
    public  String icon;

    @ApiModelProperty(value = "dto")
    public List<ServiceSourceDTO> dto;

}
