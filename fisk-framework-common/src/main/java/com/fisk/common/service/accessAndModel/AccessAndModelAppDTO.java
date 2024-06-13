package com.fisk.common.service.accessAndModel;

import lombok.Data;

import java.util.List;

@Data
public class AccessAndModelAppDTO {

    /**
     * 模型/应用/业务域id
     */
    private Integer appId;

    /**
     * 模型/应用/业务域名称
     */
    private String appName;

    /**
     * 模型/应用/业务域下的表
     */
    private List<AccessAndModelTableDTO> tables;

    /**
     * 服务类型
     * ServerTypeEnum
     */
    private Integer serverType;

    /**
     * 是否将应用简称作为schema使用
     * 否：0  false
     * 是：1  true
     */
    public Boolean whetherSchema;

    /**
     * 应用简称
     */
    public String appAbbreviation;

}
