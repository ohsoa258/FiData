package com.fisk.datagovernance.vo.dataquality.template;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description 模板VO对象
 * @date 2022/3/22 15:37
 */
@Data
public class TemplateVO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 模块类型
     */
    @ApiModelProperty(value = "模块类型")
    public int moduleType;

    /**
     * 模块名称
     */
    @ApiModelProperty(value = "模块名称")
    public String moduleName;

    /**
     * 模板类型
     */
    @ApiModelProperty(value = "模板类型")
    public int templateType;

    /**
     * 模板名称
     */
    @ApiModelProperty(value = "模板名称")
    public String templateName;

    /**
     * 模板图标
     */
    @ApiModelProperty(value = "模板图标")
    public String templateIcon;

    /**
     * 模板描述
     */
    @ApiModelProperty(value = "模板描述")
    public String templateDesc;

    /**
     * 模板状态：1 启用、2 开发中、3 禁用
     */
    @ApiModelProperty(value = "模板状态：1 启用、2 开发中、3 禁用")
    public int templateState;
}
