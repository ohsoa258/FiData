package com.fisk.datagovernance.vo.dataquality.notice;

import com.fisk.datagovernance.enums.dataquality.TemplateModulesTypeEnum;
import io.swagger.annotations.ApiModelProperty;

import java.util.stream.Stream;

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
     * 组件下所有的通知id
     */
    @ApiModelProperty(value = "组件下所有的通知id")
    public Stream<Integer> noticeIds;

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

    /**
     * 模板类型
     */
    @ApiModelProperty(value = "模板类型")
    public TemplateModulesTypeEnum templateModules;

    /**
     * 组件名称
     */
    @ApiModelProperty(value = "组件名称")
    public String moduleName;

    /**
     * 选中状态 1：选中 0：未选中
     */
    @ApiModelProperty(value = "选中状态 1：选中 0：未选中")
    public int checkedState;
}
