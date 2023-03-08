package com.fisk.dataservice.dto.tableservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class TableServiceDTO {

    public long id;

    @ApiModelProperty(value = "表名", required = true)
    public String tableName;

    @ApiModelProperty(value = "显示名称", required = true)
    public String displayName;

    @ApiModelProperty(value = "表描述")
    public String tableDes;

    @ApiModelProperty(value = "sql脚本", required = true)
    public String sqlScript;

    @ApiModelProperty(value = "目标数据源id", required = true)
    public Integer targetDbId;

    @ApiModelProperty(value = "表添加方式: 1创建新表 2选择现有表", required = true)
    public Integer addType;

    @ApiModelProperty(value = "目标表名称", required = true)
    public String targetTable;

    @ApiModelProperty(value = "来源库id", required = true)
    public Integer sourceDbId;

    @ApiModelProperty(value = "表应用ID")
    public Integer tableAppId;
}
