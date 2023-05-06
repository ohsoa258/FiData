package com.fisk.task.dto.task;

import com.fisk.common.core.enums.task.SynchronousTypeEnum;
import com.fisk.dataaccess.dto.access.DeltaTimeDTO;
import com.fisk.task.dto.MQBaseDTO;
import com.fisk.task.enums.DataClassifyEnum;
import com.fisk.task.enums.OlapTableEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author gy
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BuildNifiFlowDTO extends MQBaseDTO {
    @ApiModelProperty(value = "id")
    public Long id;
    @ApiModelProperty(value = "appId")
    public Long appId;
    @ApiModelProperty(value = "tableName")
    public String tableName;
    /*
     * 默认pg,然后doris
     * */
    @ApiModelProperty(value = "默认pg,然后doris")
    public SynchronousTypeEnum synchronousTypeEnum;
    /*
     * 表类别
     * 表类别(物理表3,事实表2,维度表1,指标表0)
     * */
    @ApiModelProperty(value = "表类别(物理表3,事实表2,维度表1,指标表0)")
    public OlapTableEnum type;
    /*
     * 数据来源类别
     * */
    @ApiModelProperty(value = "数据来源类别")
    public DataClassifyEnum dataClassifyEnum;

    /*
     * selectsql(只有toDoris,todw用得到)
     * */
    @ApiModelProperty(value = "selectsql(只有toDoris,todw用得到)")
    public String selectSql;
    /*
     * 同步方式(只有toDoris,todw用得到)
     * */
    @ApiModelProperty(value = "同步方式(只有toDoris,todw用得到)")
    public int synMode;

    /*
     * groupComponentId(管道,如果不为null,就子组里建立nifi流程)
     * */
    @ApiModelProperty(value = "groupComponentId(管道,如果不为null,就子组里建立nifi流程)")
    public String groupComponentId;
    /*
     * tb_nifi_custom_workflow_detail表的主键,用来区分同一张表被多次使用
     * */
    @ApiModelProperty(value = "tb_nifi_custom_workflow_detail表的主键,用来区分同一张表被多次使用")
    public String workflowDetailId;

    /*
     * 管道英文id(workflowId)
     * */
    @ApiModelProperty(value = "管道英文id(workflowId)")
    public String nifiCustomWorkflowId;

    /*
     * 最大单位大组id
     * */
    @ApiModelProperty(value = "最大单位大组id")
    public String groupStructureId;

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

    /*
     * 应用名称(pgtopg)
     * */
    @ApiModelProperty(value = "应用名称(pgtopg)")
    public String appName;

    /**
     * 是否同步
     */
    @ApiModelProperty(value = "是否同步")
    public Boolean openTransmission;

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
     * 建模才有的update语句
     */
    @ApiModelProperty(value = "建模才有的update语句")
    public String updateSql;

    /*
     * 接入的增量时间参数
     */
    @ApiModelProperty(value = "接入的增量时间参数")
    public List<DeltaTimeDTO> deltaTimes;

    /**
     * 版本字段语句
     */
    @ApiModelProperty(value = "版本字段语句")
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
     * 临时表名称
     */
    @ApiModelProperty(value = "临时表名称")
    public String prefixTempName;

    /**
     * 自定义脚本执行前
     */
    @ApiModelProperty(value = "自定义脚本执行前")
    public String customScriptBefore;

    /**
     * 自定义脚本执行后
     */
    @ApiModelProperty(value = "自定义脚本执行后")
    public String customScriptAfter;

    /**
     * 执行删除ods的sql语句
     */
    @ApiModelProperty(value = "执行删除ods的sql语句")
    public String whereScript;

    /**
     * 从stg抽取数据同步到ods的sql语句
     */
    @ApiModelProperty(value = "从stg抽取数据同步到ods的sql语句")
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
     * 预览Sql执行语句
     */
    @ApiModelProperty(value = "预览Sql执行语句")
    public String execSql;

    /**
     * 当Keep_number 配置天数后，这里保存删除stg表的数据的脚本语句
     * 默认：5 day
     */
    public String deleteScript;


}
