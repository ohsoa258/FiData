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

}
