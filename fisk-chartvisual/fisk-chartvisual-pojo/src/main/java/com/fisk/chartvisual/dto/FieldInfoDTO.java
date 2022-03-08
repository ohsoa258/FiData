package com.fisk.chartvisual.dto;

import lombok.Data;

/**
 * @author WangYan
 * @date 2022/3/8 15:35
 */
@Data
public class FieldInfoDTO extends TableInfoDTO {

    /**
     * 字段名称
     */
    private String field;
    /**
     * 字段类型
     */
    private String type;
    /**
     * 字段描述
     */
    private String fieldInfo;
}
