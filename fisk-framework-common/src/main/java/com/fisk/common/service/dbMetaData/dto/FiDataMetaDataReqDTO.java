package com.fisk.common.service.dbMetaData.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author dick
 * @version 1.0
 * @description FiData数据源查询请求DTO
 * @date 2022/6/16 17:18
 */
@Data
public class FiDataMetaDataReqDTO {
    @ApiModelProperty(value = "FiData数据源id")
    public String dataSourceId;

    @ApiModelProperty(value = "FiData数据源名称")
    public String dataSourceName;
}
