package com.fisk.datagovernance.vo.dataquality.external;

import com.fisk.datagovernance.enums.dataquality.ModuleTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 元数据质量规则VO
 * @date 2023/7/31 13:31
 */
@Data
public class MetaDataQualityRuleVO {
    /**
     * 质量规则业务模块
     */
    @ApiModelProperty(value = "质量规则业务模块")
    public ModuleTypeEnum moduleTypeEnum;

    /**
     * 元数据表规则
     */
    @ApiModelProperty(value = "元数据表规则")
    public List<MetaDataTableRuleVO> tableRuleList;

    /**
     * 元数据字段规则
     */
    @ApiModelProperty(value = "元数据字段规则")
    public List<MetaDataFieldRuleVO> fieldRuleList;
}
