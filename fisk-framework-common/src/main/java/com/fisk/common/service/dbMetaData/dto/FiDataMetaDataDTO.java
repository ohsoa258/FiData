package com.fisk.common.service.dbMetaData.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author dick
 * @version 1.0
 * @description FiData元数据树形结构DTO
 * @date 2022/6/15 11:23
 */
@Data
public class FiDataMetaDataDTO {
    @ApiModelProperty(value = "FiData数据源id", required = true)
    public int dataSourceId ;

    @ApiModelProperty(value = "FiData数据源Tree")
    public List<FiDataMetaDataTreeDTO> children;
}
