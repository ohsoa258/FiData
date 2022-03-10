package com.fisk.chartvisual.dto;

import lombok.Data;

/**
 * @author WangYan
 * @date 2022/3/10 10:51
 */
@Data
public class DsTableFieldDTO {

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
