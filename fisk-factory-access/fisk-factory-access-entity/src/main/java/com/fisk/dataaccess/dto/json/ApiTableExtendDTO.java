package com.fisk.dataaccess.dto.json;

import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/1/21 11:51
 */
@Data
public class ApiTableExtendDTO {
    @ApiModelProperty(value = "api下的表")
    public String tableName;

    @ApiModelProperty(value = "pid")
    public Boolean pid;

    @ApiModelProperty(value = "父表名称")
    public String fatherTableName;

    /**
     * 表字段集合
     */
    @ApiModelProperty(value = "表字段集合")
    public List<TableFieldsDTO> list;

    /**
     * 预留待使用
     */
    @ApiModelProperty(value = "预留待使用")
    public List<JsonSchema> jsonSchemaList;

    /**
     * 子级表名
     */
    @ApiModelProperty(value = "子级表名")
    public List<ApiTableExtendDTO> childTable;
}
