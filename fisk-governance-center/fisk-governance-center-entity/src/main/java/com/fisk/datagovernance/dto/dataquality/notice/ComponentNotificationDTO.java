package com.fisk.datagovernance.dto.dataquality.notice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 组件通知关联DTO
 * @date 2022/3/24 14:31
 */
public class ComponentNotificationDTO {
    /**
     * 组件id
     */
    @ApiModelProperty(value = "组件id")
    public int moduleId;

    /**
     * 通知id
     */
    @ApiModelProperty(value = "通知id")
    public int noticeId;

    /**
     * 模板id
     */
    @ApiModelProperty(value = "模板id")
    public int templateId;
}
