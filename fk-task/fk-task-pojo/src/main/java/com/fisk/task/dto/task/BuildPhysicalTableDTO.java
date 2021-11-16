package com.fisk.task.dto.task;

import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.task.enums.DbTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class BuildPhysicalTableDTO {
    public List<TableFieldsDTO> tableFieldsDTOS;
    public String appAbbreviation;
    public String selectSql;
    public String tableName;
    /**
     * 驱动类型
     */
    public DbTypeEnum driveType;
}
