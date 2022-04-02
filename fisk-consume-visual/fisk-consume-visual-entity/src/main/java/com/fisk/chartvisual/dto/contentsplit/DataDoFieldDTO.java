package com.fisk.chartvisual.dto.contentsplit;

import com.fisk.chartvisual.enums.NodeTypeEnum;
import com.fisk.chartvisual.enums.FieldTypeEnum;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class DataDoFieldDTO {
    private Integer fieldId;
    private String fieldName;
    private String where;
    private String whereValue;
    private FieldTypeEnum fieldType;
    private String tableName;
    /**
     * 是否维度 0 否  1 是维度
     */
    public NodeTypeEnum dimension;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 指定时间
     */
    private String[] specifiedTime;
}
