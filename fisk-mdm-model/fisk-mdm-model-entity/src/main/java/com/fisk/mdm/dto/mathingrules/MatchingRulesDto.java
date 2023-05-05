package com.fisk.mdm.dto.mathingrules;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class MatchingRulesDto {
    /**
     * 模型Id
     */
    @ApiModelProperty(value = "模型Id")
    public Integer modelId;
    /**
     * 实体Id
     */
    @ApiModelProperty(value = "实体Id")
    public Integer entityId;
    /**
     * 最低阈值
     */
    @ApiModelProperty(value = "最低阈值")
    public Integer lowThreshold;
    /**
     * 最高阈值
     */
    @ApiModelProperty(value = "最高阈值")
    public Integer highThreshold;
    /**
     * 匹配字段类型 1 全部字段 2 自定义字段
     */
    @ApiModelProperty(value = "匹配字段类型 1 全部字段 2 自定义字段")
    public Integer matchFieldType;
    /**
     * 匹配字段
     */
    @ApiModelProperty(value = "匹配字段")
    public List<MatchingRulesFiledDto> matchingRulesFiledDtoList;
    /**
     * 匹配源系统
     */
    @ApiModelProperty(value = "匹配源系统")
    public List<SourceSystemMappingDto> sourceSystemMappingDtoList;
}
