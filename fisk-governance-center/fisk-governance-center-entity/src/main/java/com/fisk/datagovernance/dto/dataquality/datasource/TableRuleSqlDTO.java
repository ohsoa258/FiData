package com.fisk.datagovernance.dto.dataquality.datasource;

import com.fisk.datagovernance.enums.dataquality.TemplateTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description 表规则查询DTO
 * @date 2022/8/22 14:18
 */
@Data
public class TableRuleSqlDTO {
    /**
     * 表名称
     */
    @ApiModelProperty(value = "表名称")
    public String tableName;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    /**
     * 字段名称集合
     */
    @ApiModelProperty(value = "字段名称集合")
    public List<String> fieldNames;

    /**
     * 模板类型
     */
    @ApiModelProperty(value = "模板类型")
    public TemplateTypeEnum templateTypeEnum;

    /**
     * sql语句
     */
    @ApiModelProperty(value = "sql语句")
    public String sql;

    /**
     * 聚合函数
     */
    public String fieldAggregate;

    /**
     * 波动阈值
     */
    public int thresholdValue;
}
