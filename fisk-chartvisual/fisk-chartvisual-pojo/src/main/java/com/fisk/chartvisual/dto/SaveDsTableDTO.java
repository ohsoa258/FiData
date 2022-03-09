package com.fisk.chartvisual.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author WangYan
 * @date 2022/3/9 17:27
 */
@Data
public class SaveDsTableDTO {

    /**
     * 数据源id
     */
    @NotNull
    private Integer dataSourceId;
    /**
     * 表名
     */
    @NotNull
    private String tableName;
    /**
     * 源字段
     */
    private String sourceField;
    /**
     * 目标字段名
     */
    private String targetField;
    /**
     * 源字段类型
     */
    private String sourceFieldType;
    /**
     * 目标字段类型
     */
    private String targetFieldType;
    /**
     * 字段描述
     */
    private String describe;
}
