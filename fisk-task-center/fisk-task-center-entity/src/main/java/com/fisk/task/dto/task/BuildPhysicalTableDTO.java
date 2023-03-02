package com.fisk.task.dto.task;

import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.dataaccess.dto.table.TableFieldsDTO;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.dto.modelpublish.ModelPublishTableDTO;
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

    /**
     * 版本号和存储过程
     */
    public ModelPublishTableDTO modelPublishTableDTO;

    /*
    * 同步方式
    * */
    public int syncMode;

    /*
    * 查询范围开始时间
    * */
    public String queryStartTime;

    /*
    * 查询范围结束时间
    * */
    public String queryEndTime;

    public boolean openTransmission;

    /**
     * 应用类型
     */
    public Integer appType;
    /**
     * api下所有表
     */
    public List<String> apiTableNames;

    /**
     * api id
     */
    public Long apiId;

    /**
     * 是否是表格同步
     */
    public boolean excelFlow;

    /**
     * 是否是sftp
     */
    public boolean sftpFlow;
    /**
     * 是否是schema结构
     */
    public boolean whetherSchema;


    /*
     * 接入的增量时间参数
     */
    public List<DeltaTimeDTO> deltaTimes;


    /**
     * 获取版本的语句
     */
    public String generateVersionSql;
    /**
     * 单个数据流文件加载最大数据行
     */
    public int maxRowsPerFlowFile;
    /**
     * 单次从结果集中提取的最大数据行
     */
    public int fetchSize;

    /**
     * sheet名
     */
    public String sheetName;

    /**
     * 数据来源id
     */
    public Integer dataSourceDbId;

    /**
     * 目标数据源id
     */
    public Integer targetDbId;

    /**
     * 执行删除ods的sql语句
     */
    public String whereScript;

    /**
     * 从stg加载数据同步到ods的sql语句
     */
    public String syncStgToOdsSql;

    /**
     * 临时表(建模temp_tablename)建表语句
     */
    public String buildTableSql;
}
