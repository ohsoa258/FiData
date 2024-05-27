package com.fisk.common.core.utils.dbutils.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DataBaseInfoDTO {

    @ApiModelProperty(value = "数据库名")
    public String dbName;

    @ApiModelProperty(value = "表名列表")
    public List<TableNameDTO> tableNameList;

}
