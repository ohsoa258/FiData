package com.fisk.dataaccess.dto.table;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class TableNameTreeDTO {

    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public long id;
    /**
     * 父id
     */
    @ApiModelProperty(value = "pid")
    public long pid;

    /**
     * 物理表名
     */
    @ApiModelProperty(value = "物理表名称", required = true)
    public String tableName;
    /**
     * 1: 数据接入; 2:数据建模
     */
    @ApiModelProperty(value = "1: 数据接入; 2:数据建模", required = true)
    public int flag;
    /**
     * 0:实时  1:非实时
     */
    @ApiModelProperty(value = "0:实时  1:非实时", required = true)
    public int appType;
}
