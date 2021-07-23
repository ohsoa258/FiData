package com.fisk.system.dto;

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
    public String serveCnName;

    /**
     *服务url
     */
    public  String serveUrl;

    /**
     *服务图标
     */
    public  String icon;

    public List<ServiceSourceDTO> dto;

}
