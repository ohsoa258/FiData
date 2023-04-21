package com.fisk.datamanagement.dto.search;

import com.fisk.datamanagement.dto.classification.ClassificationDTO;
import com.fisk.datamanagement.dto.entity.EntityAttributesDTO;
import com.fisk.datamanagement.dto.glossary.GlossaryDTO;
import com.fisk.datamanagement.dto.label.LabelDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName:
 * @Author: 湖~Zero
 * @Date: 2023
 * @Copyright: 2023 by 湖~Zero
 * @Description:
 **/
@Data
public class EntitiesDTO {

    /**
     * 显示名称
     */
    @ApiModelProperty(value = "显示名称")
    public String displayText;
    /**
     * 业务分类信息
     */
    @ApiModelProperty(value = "业务分类信息")
    public List<ClassificationDTO> classifications;
    /**
     * 关联业务分类集合
     */
    @ApiModelProperty(value = "关联业务分类集合")
    public List<String> classificationNames;
    /**
     * 关联术语集合
     */
    @ApiModelProperty(value = "关联术语集合")
    public List<String> meaningNames;
    /**
     * 实体类型
     */
    @ApiModelProperty(value = "实体类型")
    public String typeName;

    @ApiModelProperty(value = "guid")
    public String guid;
    /**
     * 属性信息
     */
    @ApiModelProperty(value = "属性信息")
    public EntityAttributesDTO attributes;
    /**
     * 术语信息
     */
    @ApiModelProperty(value = "术语信息")
    public List<GlossaryDTO> meanings;

    @ApiModelProperty(value = "状态")
    public String status;

    @ApiModelProperty(value = "是否完整")
    public String isIncomplete;
    /**
     * 关联属性标签
     */
    @ApiModelProperty(value = "关联属性标签")
    public List<LabelDTO> labels;

}