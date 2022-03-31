package com.fisk.common.service.dbMetaData.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 *     表结构对象
 * </p>
 * @author Lock
 */
@Data
public class TableStructureDTO {
    /**
     * 字段名
     */
    @ApiModelProperty(value = "字段名", required = true)
    public String fieldName;
    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型", required = true)
    public String fieldType;
    /**
     * 字段长度
     */
    @ApiModelProperty(value = "字段长度", required = true)
    public int fieldLength;

    /**
     * 字段描述
     */
    @ApiModelProperty(value = "字段描述", required = true)
    public String fieldDes;
}
