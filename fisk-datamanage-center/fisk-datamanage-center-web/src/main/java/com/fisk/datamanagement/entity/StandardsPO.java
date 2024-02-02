package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangjian
 * @date 2023-11-20 13:56:24
 */
@TableName("tb_standards")
@Data
public class StandardsPO extends BasePO {

    @ApiModelProperty(value = "menu_id")
    private Integer menuId;

    @ApiModelProperty(value = "中文名称")
    private String chineseName;

    @ApiModelProperty(value = "英文名称")
    private String englishName;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "字段类型")
    private String fieldType;

    @ApiModelProperty(value = "数据元编号")
    private String datametaCode;

    @ApiModelProperty(value = "质量规则")
    private String qualityRule;

    @ApiModelProperty(value = "值域范围类型 1数据集 2数值 3数值范围")
    private Integer valueRangeType;

    @ApiModelProperty(value = "符号")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String symbols;

    @ApiModelProperty(value = "值域范围")
    private String valueRange;

    @ApiModelProperty(value = "值域范围")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String valueRangeMax;
}
