package com.fisk.dataaccess.dto.json;

import com.fisk.dataaccess.dto.TableFieldsDTO;
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
public class ApiTableDTO {
    @ApiModelProperty(value = "api下的表")
    public String tableName;

    @ApiModelProperty(value = "根节点")
    public int pid;

    /**
     * 表字段集合
     */
    public List<TableFieldsDTO> list;

    @ApiModelProperty(value = "子级表名")
    public List<String> childTableName;
}
