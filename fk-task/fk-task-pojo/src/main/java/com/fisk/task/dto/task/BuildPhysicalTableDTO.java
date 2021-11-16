package com.fisk.task.dto.task;

import com.fisk.dataaccess.dto.TableFieldsDTO;
import lombok.Data;

import java.util.List;

@Data
public class BuildPhysicalTableDTO {
    public List<TableFieldsDTO> tableFieldsDTOS;
    public String selectSql;
    public String tableName;
}
