package com.fisk.chartvisual.entity;

import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/3/4 10:57
 */
@Data
public class DsTableFieldPO extends BasePO {

    /**
     * 表名id
     */
    private Integer tableInfoId;
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
