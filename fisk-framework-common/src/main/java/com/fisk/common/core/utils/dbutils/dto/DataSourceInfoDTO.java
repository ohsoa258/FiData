package com.fisk.common.core.utils.dbutils.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DataSourceInfoDTO {

    @ApiModelProperty(value = "数据源ID")
    public Integer dbId;
    @ApiModelProperty(value = "数据源名称")
    public String dataSourceName;

    @ApiModelProperty(value = "数据库列表")
    public List<DataBaseInfoDTO> dataBaseInfoDTOList;

}
