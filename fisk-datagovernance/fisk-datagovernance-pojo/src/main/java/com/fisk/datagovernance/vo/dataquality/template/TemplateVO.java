package com.fisk.datagovernance.vo.dataquality.template;

import com.fisk.datagovernance.enums.dataquality.TemplateModulesTypeEnum;
import com.fisk.datagovernance.enums.dataquality.TemplateTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

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
     * 模板名称
     */
    @ApiModelProperty(value = "模板名称")
    public String templatenName;

    /**
     * 模板模块
     */
    @ApiModelProperty(value = "模板模块")
    public TemplateModulesTypeEnum templateModules;

    /**
     * 模板类型
     */
    @ApiModelProperty(value = "模板类型")
    public TemplateTypeEnum templateType;

    /**
     * 模板描述
     */
    @ApiModelProperty(value = "模板描述")
    public String templateDesc;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    public LocalDateTime createTime;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    public String createUser;
}
