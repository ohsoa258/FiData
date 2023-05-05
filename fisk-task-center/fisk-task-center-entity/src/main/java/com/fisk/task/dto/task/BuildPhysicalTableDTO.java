package com.fisk.task.dto.task;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
import com.fisk.task.enums.DbTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class BuildPhysicalTableDTO extends MQBaseDTO {
    @ApiModelProperty(value = "表字段DTOS")
    public List<TableFieldsDTO> tableFieldsDTOS;
    @ApiModelProperty(value = "应用缩写")
    public String appAbbreviation;
    @ApiModelProperty(value = "选择sql")
    public String selectSql;
    @ApiModelProperty(value = "表名")
    public String tableName;
    @ApiModelProperty(value = "应用id")
    public String appId;
    @ApiModelProperty(value = "数据库id")
    public String dbId;

    /**
     * 驱动类型
     */
    @ApiModelProperty(value = "驱动类型")
    public DbTypeEnum driveType;

    /**
     * 版本号和存储过程
     */
    @ApiModelProperty(value = "版本号和存储过程")
    public ModelPublishTableDTO modelPublishTableDTO;

    /*
    * 同步方式
    * */
    @ApiModelProperty(value = "同步方式")
    public int syncMode;

    /*
    * 查询范围开始时间
    * */
    @ApiModelProperty(value = "查询范围开始时间")
    public String queryStartTime;

    /*
    * 查询范围结束时间
    * */
    @ApiModelProperty(value = "查询范围结束时间")
    public String queryEndTime;

    @ApiModelProperty(value = "开式变速器")
    public boolean openTransmission;

    /**
     * 应用类型
     */
    @ApiModelProperty(value = "应用类型")
    public Integer appType;
    /**
     * api下所有表
     */
    @ApiModelProperty(value = "api下所有表")
    public List<String> apiTableNames;

    /**
     * api id
     */
    @ApiModelProperty(value = "api id")
    public Long apiId;

    /**
     * 是否是表格同步
     */
    @ApiModelProperty(value = "是否是表格同步")
    public boolean excelFlow;

    /**
     * 是否是sftp
     */
    @ApiModelProperty(value = "是否是sftp")
    public boolean sftpFlow;
    /**
     * 是否是schema结构
     */
    @ApiModelProperty(value = "是否是schema结构")
    public boolean whetherSchema;


    /*
     * 接入的增量时间参数
     */
    @ApiModelProperty(value = "接入的增量时间参数")
    public List<DeltaTimeDTO> deltaTimes;


    /**
     * 获取版本的语句
     */
    @ApiModelProperty(value = "获取版本的语句")
    public String generateVersionSql;
    /**
     * 单个数据流文件加载最大数据行
     */
    @ApiModelProperty(value = "单个数据流文件加载最大数据行")
    public int maxRowsPerFlowFile;
    /**
     * 单次从结果集中提取的最大数据行
     */
    @ApiModelProperty(value = "单次从结果集中提取的最大数据行")
    public int fetchSize;

    /**
     * sheet名
     */
    @ApiModelProperty(value = "sheet名")
    public String sheetName;

    /**
     * 数据来源id
     */
    @ApiModelProperty(value = "数据来源id")
    public Integer dataSourceDbId;

    /**
     * 目标数据源id
     */
    @ApiModelProperty(value = "目标数据源id")
    public Integer targetDbId;

    /**
     * 执行删除ods的sql语句
     */
    @ApiModelProperty(value = "执行删除ods的sql语句")
    public String whereScript;

    /**
     * 从stg加载数据同步到ods的sql语句
     */
    @ApiModelProperty(value = "从stg加载数据同步到ods的sql语句")
    public String syncStgToOdsSql;

    /**
     * 临时表(建模temp_tablename)建表语句
     */
    @ApiModelProperty(value = "临时表(建模temp_tablename)建表语句")
    public String buildTableSql;

    /**
     * 发布历史id
     */
    @ApiModelProperty(value = "发布历史id")
    public Long tableHistoryId;

    /**
     * 覆盖脚本
     */
    @ApiModelProperty(value = "覆盖脚本")
    public String coverScript;

    /**
     * 当Keep_number 配置天数后，这里保存删除stg表的数据的脚本语句
     * 默认：5 day
     */
    @ApiModelProperty(value = "当Keep_number 配置天数后，这里保存删除stg表的数据的脚本语句")
    public String deleteStgScript;

}
