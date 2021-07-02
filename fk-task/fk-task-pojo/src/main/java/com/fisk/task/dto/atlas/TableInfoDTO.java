package com.fisk.task.dto.atlas;

import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

import java.util.List;

/**
 * @author:DennyHui
 * CreateTime: 2021/7/1 14:59
 * Description:
 */
@Data
public class TableInfoDTO extends MQBaseDTO {
    public String tableName;
    public List<TableColumnInfoDTO> columns;
}
