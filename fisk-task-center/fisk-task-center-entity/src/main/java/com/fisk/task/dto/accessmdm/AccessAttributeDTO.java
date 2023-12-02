package com.fisk.task.dto.accessmdm;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wangjian
 */
@Data
public class AccessAttributeDTO {
    public long id;

    private Integer accessId;
    /**
     * 源表名称
     */
    private String sourceTableName;

    /**
     * 源字段名称
     */
    private String sourceFieldName;

    /**
     * 属性id
     */
    private Integer attributeId;

    /**
     * 业务主键
     */
    private Integer businessKey;
    /**
     * 字段名字
     */
    public String fieldName;
    /**
     * 数据类型
     */
    private String dataType;

    public String dataTypeEnDisplay;

    /**
     * 数据类型长度
     */
    private Integer dataTypeLength;

    /**
     * 数据类型小数点长度
     */
    private Integer dataTypeDecimalLength;

    /**
     * 数据格式id
     */
    private Integer dataTypeFormatId;
    /**
     * mdm字段名字
     */
    public String mdmFieldName;
    /**
     * 映射类型1:基础映射:2基于域映射
     */
    public Integer mappingType;
}
