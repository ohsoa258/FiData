package com.fisk.dataaccess.dto.table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class TableNameDTO {

    /**
     * 表名
     */
    @ApiModelProperty(value = "物理表名称", required = true)
    public String tableName;

    /**
     * 表id
     */
    @ApiModelProperty(value = "物理表id", required = true)
    public long id;

    /**
     * 表字段
     */
    public List<FieldNameDTO> field;

}
