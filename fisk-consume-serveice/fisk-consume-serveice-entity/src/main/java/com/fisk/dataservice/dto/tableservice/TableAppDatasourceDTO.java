package com.fisk.dataservice.dto.tableservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TableAppDatasourceDTO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;

    /**
     * 表应用id
     */
    @ApiModelProperty(value = "表应用id")
    public int tableAppId;

    /**
     * 数据源类型 1源数据库数据源 2目标数据库数据源
     */
    @ApiModelProperty(value = "数据源类型 1源数据库数据源 2目标数据库数据源")
    public int datasourceType;

    /**
     * FiData数据源id
     */
    @ApiModelProperty(value = "FiData数据源id")
    public int datasourceId;
}
