package com.fisk.datamodel.dto.tableconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class SourceTableDTO {

    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "表名称")
    public String tableName;

    @ApiModelProperty(value = "类型")
    public int type;

    @ApiModelProperty(value = "表详细信息")
    public String tableDes;

    @ApiModelProperty(value = "sql脚本")
    public String sqlScript;

    @ApiModelProperty(value = "字段列表")
    public List<SourceFieldDTO> fieldList;
}
