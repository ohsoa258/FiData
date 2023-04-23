package com.fisk.task.dto.doris;

import com.fisk.task.dto.MQBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author:DennyHui
 * CreateTime: 2021/7/1 14:59
 * Description:
 */
@Data
public class TableInfoDTO extends MQBaseDTO {
    @ApiModelProperty(value = "表名")
    public String tableName;
    @ApiModelProperty(value = "列")
    public List<TableColumnInfoDTO> columns;
}
