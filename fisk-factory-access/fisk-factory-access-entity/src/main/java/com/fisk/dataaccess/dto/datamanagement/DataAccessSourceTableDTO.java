package com.fisk.dataaccess.dto.datamanagement;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 * @version 2.0
 * @description
 * @date 2022/1/6 14:50
 */
@Data
public class DataAccessSourceTableDTO {

    @ApiModelProperty(value = "物理表id")
    public long id;

    @ApiModelProperty(value = "物理表名称")
    public String tableName;

    @ApiModelProperty(value = "物理表描述")
    public String tableDes;

    @ApiModelProperty(value = "物理表id")
    public Integer appId;

    @ApiModelProperty(value = "数据源id")
    public Integer dataSourceId;

    @ApiModelProperty(value = "应用缩写")
    public String appAbbreviation;

    @ApiModelProperty(value = "sql脚本")
    public String sqlScript;

    @ApiModelProperty(value = "驱动类型")
    public String driveType;

    /**
     * 表字段集合
     */
    @ApiModelProperty(value = "表字段集合")
    public List<DataAccessSourceFieldDTO> list;
}
