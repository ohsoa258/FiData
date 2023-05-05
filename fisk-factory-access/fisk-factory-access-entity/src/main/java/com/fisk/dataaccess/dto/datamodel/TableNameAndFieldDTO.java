package com.fisk.dataaccess.dto.datamodel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class TableNameAndFieldDTO {


    public String tableName;

    /**
     * 返回给前端的唯一标记
     */
    @ApiModelProperty(value = "返回给前端的唯一标记")
    public int tag;

    /**
     * 表字段
     */
    @ApiModelProperty(value = "表字段")
    public List<String> fields;
}
