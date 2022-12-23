package com.fisk.task.dto.task;

import com.fisk.dataservice.dto.tablefields.TableFieldDTO;
import com.fisk.dataservice.dto.tablesyncmode.TableSyncModeDTO;
import com.fisk.task.dto.MQBaseDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BuildTableServiceDTO extends MQBaseDTO {

    /**
     * 表名
     */
    public String tableName;

    /**
     * 目标数据源id
     */
    public Integer targetDbId;

    /**
     * 数据源id
     */
    public Integer dataSourceId;

    /**
     * 脚本
     */
    public String sqlScript;

    /**
     * 目标表名称
     */
    public String targetTable;

    /**
     * 表添加方式: 1创建新表 2选择现有表
     */
    public Integer addType;

    /**
     * 同步配置
     */
    public TableSyncModeDTO syncModeDTO;

    /**
     * 字段集合
     */
    public List<TableFieldDTO> fieldDtoList;


}
