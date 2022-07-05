package com.fisk.common.service.dbMetaData.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description FiData 表元数据查询
 * @date 2022/6/16 17:18
 */
@Data
public class FiDataTableMetaDataReqDTO {
    /**
     * FiData数据源ID
     * 1:DW
     * 2:ODS
     * 3:MDM
     * 4:OLAP
     */
    @ApiModelProperty(value = "FiData数据源ID")
    public String dataSourceId;

    /**
     * 表ID集合
     */
    @ApiModelProperty(value = "表ID集合")
    public List<String> tableId;
}
