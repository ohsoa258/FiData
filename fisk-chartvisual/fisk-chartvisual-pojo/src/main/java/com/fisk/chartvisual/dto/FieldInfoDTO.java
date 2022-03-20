package com.fisk.chartvisual.dto;

import com.fisk.chartvisual.enums.isExistTypeEnum;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/3/8 15:35
 */
@Data
public class FieldInfoDTO {

    /**
     * 字段名称
     */
    private String field;
    /**
     * 字段类型
     */
    private String type;
    /**
     * 目标字段类型
     */
    private String targetType;
    /**
     * 字段描述
     */
    private String fieldInfo;
    /**
     * 表是否存在库里
     */
    private isExistTypeEnum fieldIsExist;
}
