package com.fisk.dataservice.dto;

import com.fisk.dataservice.enums.DataDoFieldTypeEnum;
import lombok.Data;

import java.util.Date;

/**
 * @author Lock
 */
@Data
public class DataDoFieldDTO {
    private Integer fieldId;
    private String fieldName;
    private String where;
    private String whereValue;
    private DataDoFieldTypeEnum fieldType;
    private String tableName;
    /**
     * 是否维度 0 否  1 是维度
     */
    public int dimension;

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
