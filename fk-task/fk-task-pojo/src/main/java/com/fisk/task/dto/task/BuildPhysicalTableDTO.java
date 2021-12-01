package com.fisk.task.dto.task;

import com.fisk.dataaccess.dto.TableFieldsDTO;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.enums.DbTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class BuildPhysicalTableDTO extends MQBaseDTO {
    public List<TableFieldsDTO> tableFieldsDTOS;
    public String appAbbreviation;
    public String selectSql;
    public String tableName;
    public String appId;
    public String dbId;
    /**
     * 驱动类型
     */
    public DbTypeEnum driveType;
}
