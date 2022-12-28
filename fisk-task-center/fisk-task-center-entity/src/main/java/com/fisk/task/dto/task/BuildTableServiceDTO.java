package com.fisk.task.dto.task;

import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.dataservice.dto.tablefields.TableFieldDTO;
import com.fisk.dataservice.dto.tablesyncmode.TableSyncModeDTO;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BuildTableServiceDTO extends MQBaseDTO {

    /**
     * 表id
     */
    public long id;
    /**
     * 表名
     */
    public String tableName;

    /**
     * schema名称
     */
    public String schemaName;

    /**
     * 目标数据源id
     */
    public Integer targetDbId;

    /**
     * 数据源id
     */
    public Integer dataSourceId;

    /**
     * 查询脚本
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

    /**
     * 同步类型
     */
    public SynchronousTypeEnum synchronousTypeEnum = SynchronousTypeEnum.TOEXTERNALDB;

    /**
     * 数据类别
     */
    public DataClassifyEnum dataClassifyEnum = DataClassifyEnum.DATASERVICES;

    /**
     * 表类别
     */
    public OlapTableEnum olapTableEnum = OlapTableEnum.DATASERVICES;


}
