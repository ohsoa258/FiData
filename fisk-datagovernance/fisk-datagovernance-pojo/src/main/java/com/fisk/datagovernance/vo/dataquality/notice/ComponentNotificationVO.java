package com.fisk.datagovernance.vo.dataquality.notice;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author dick
 * @version 1.0
 * @description 组件通知关联VO
 * @date 2022/3/22 15:37
 */
public class ComponentNotificationVO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

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
}
