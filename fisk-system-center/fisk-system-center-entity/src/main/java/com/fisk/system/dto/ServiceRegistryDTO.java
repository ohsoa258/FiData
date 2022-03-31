package com.fisk.system.dto;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ServiceRegistryDTO {

    public  int id;

    /**
     *服务code
    */
    public  String serveCode;

    /**
    *上一级服务code
    */
    public  String parentServeCode;

    /**
    *服务中文名称
    */
    public  String serveCnName;

    /**
    *服务英文名称
    */
    public  String serveEnName;

    /**
    *服务url
    */
    public  String serveUrl;

    /**
    *服务图标
     */
    public  String icon;

    /**
     *排序号
     */
    public  int sequenceNo;

    /**
    *父级服务下一级服务list
    */
    public List<ServiceRegistryDTO> dtos;

}
