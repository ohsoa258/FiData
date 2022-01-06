package com.fisk.dataaccess.dto.datamanagement;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 * @version 2.0
 * @description
 * @date 2022/1/6 14:52
 */
@Data
public class DataAccessSourceFieldDTO {

    @ApiModelProperty(value = "字段id")
    public long id;

    @ApiModelProperty(value = "字段名称")
    public String fieldName;

    @ApiModelProperty(value = "字段描述")
    public String fieldDes;

    @ApiModelProperty(value = "字段类型")
    public String fieldType;

    @ApiModelProperty(value = "字段长度")
    public int fieldLength;

    @ApiModelProperty(value = "字段是否业务主键")
    public int primaryKey;

}
