package com.fisk.dataaccess.dto.v3;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 */
@Data
public class DatabaseDTO {
    @ApiModelProperty(value = "类型")
    public int type;
    /**
     * 数据库名称
     */
    @ApiModelProperty(value = "数据库名称")
    public String databaseName;
    /**
     * 表列表
     */
    @ApiModelProperty(value = "表列表")
    public List<TableDTO> tableList;
}
