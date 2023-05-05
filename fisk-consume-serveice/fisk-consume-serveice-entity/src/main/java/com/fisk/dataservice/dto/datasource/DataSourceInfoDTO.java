package com.fisk.dataservice.dto.datasource;

import com.fisk.common.core.utils.dbutils.dto.TableNameDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class DataSourceInfoDTO {

    @ApiModelProperty(value = "数据库ID")
    public Integer dbId;

    @ApiModelProperty(value = "数据库名")
    public String dbName;

    @ApiModelProperty(value = "表名列表")
    public List<TableNameDTO> tableNameList;

}
