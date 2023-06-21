package com.fisk.task.dto.task;

import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.dataservice.dto.tablefields.TableFieldDTO;
import com.fisk.dataservice.dto.tablesyncmode.TableSyncModeDTO;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "表id")
    public long id;
    /**
     * 表名
     */
    @ApiModelProperty(value = "表名")
    public String tableName;

    /**
     * schema名称
     */
    @ApiModelProperty(value = "schema名称")
    public String schemaName;

    /**
     * 目标数据源id
     */
    @ApiModelProperty(value = "目标数据源id")
    public Integer targetDbId;

    /**
     * 数据源id
     */
    @ApiModelProperty(value = "数据源id")
    public Integer dataSourceId;

    /**
     * 查询脚本
     */
    @ApiModelProperty(value = "查询脚本")
    public String sqlScript;

    /**
     * 目标表名称
     */
    @ApiModelProperty(value = "目标表名称")
    public String targetTable;

    /**
     * 表添加方式: 1创建新表 2选择现有表
     */
    @ApiModelProperty(value = "表添加方式: 1创建新表 2选择现有表")
    public Integer addType;

    /**
     * 同步配置
     */
    @ApiModelProperty(value = "同步配置")
    public TableSyncModeDTO syncModeDTO;

    /**
     * 字段集合
     */
    @ApiModelProperty(value = "字段集合")
    public List<TableFieldDTO> fieldDtoList;

    /**
     * 同步类型
     */
    @ApiModelProperty(value = "同步类型")
    public SynchronousTypeEnum synchronousTypeEnum = SynchronousTypeEnum.TOEXTERNALDB;

    /**
     * 数据类别
     */
    @ApiModelProperty(value = "数据类别")
    public DataClassifyEnum dataClassifyEnum = DataClassifyEnum.DATASERVICES;

    /**
     * 表类别
     */
    @ApiModelProperty(value = "表类别")
    public OlapTableEnum olapTableEnum = OlapTableEnum.DATASERVICES;
    /**
     * 表应用ID
     */
    @ApiModelProperty(value = "表应用ID")
    public Integer tableAppId;
    /**
     * 表应用名称
     */
    @ApiModelProperty(value = "表应用名称")
    public String tableAppName;
    /**
     * 表应用描述
     */
    @ApiModelProperty(value = "表应用描述")
    public String tableAppDesc;
}
