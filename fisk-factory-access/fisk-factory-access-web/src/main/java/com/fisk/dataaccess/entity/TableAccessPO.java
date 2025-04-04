package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.dataaccess.enums.ScanStartupModeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author Lock
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_table_access")
public class TableAccessPO extends BasePO implements Serializable {
    /**
     * 父id
     */
    public int pid;

    /**
     * tb_app_registration表id
     */
    public Long appId;

    /**
     * apiId
     */
    public Long apiId;


    /**
     * 物理表名
     */
    public String tableName;

    /**
     * 物理表描述
     */
    public String tableDes;

    /**
     * 如果是实时物理表，需要提供数据同步地址
     */
    public String syncSrc;

    /**
     * 0是实时物理表，1是非实时物理表
     */
    public Integer isRealtime;
    /**
     * 0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布  4:已终止
     */
    public Integer publish;

    /**
     * SQL脚本or文件全限定名称
     */
    public String sqlScript;

    /**
     * excel sheet页名称
     */
    public String sheet;

    /**
     * excel sheet数据开始读取行数
     */
    public Integer startLine;

    /**
     * 发布错误信息
     */
    public String publishErrorMsg;

    /**
     * oracle-cdc管道名称
     */
    public String pipelineName;

    /**
     * oracle-cdc检查点时间
     */
    public Integer checkPointInterval;

    /**
     * oracle-cdc检查点时间单位
     */
    public String checkPointUnit;

    /**
     * 0:从最开始读 1:从最新的读
     */
    public ScanStartupModeEnum scanStartupMode;

    /**
     * oracle-cdc任务id
     */
    public String jobId;

    /**
     * 是否使用已存在表
     */
    public Boolean useExistTable;

    /**
     * 物理表显示名称
     */
    public String displayName;

    /**
     * stg数据保留天数
     */
    public String keepNumber;

    /**
     * 应用数据源id
     */
    public Integer appDataSourceId;

    /**
     * 业务时间覆盖的where条件
     */
    public String whereScript;

    /**
     * 覆盖脚本
     */
    public String coverScript;

    /**
     * 当Keep_number 配置天数后，这里保存删除stg表的数据的脚本语句
     * 默认：5 day
     */
    public String deleteStgScript;

    /**
     * 是否是重点接口 0否，1是
     */
    public Integer isImportantInterface;

    /**
     * sapbw-mdx语句集合
     */
    public String mdxSqlList;

    /**
     * hudi入仓配置是否开启cdc
     */
    public Integer ifOpenCdc;

    /**
     * Flink Source Sql
     */
    public String sourceSql;

    /**
     * Flink Sink Sql
     */
    public String sinkSql;

    /**
     * Flink Insert Sql
     */
    public String insertSql;

    /**
     * Flink jobid
     */
    public String flinkJobid;

    /**
     * powerbi 数据集id
     */
    public String pbiDatasetId;

    /**
     * pbi 查询时所用的用户名
     */
    public String pbiUsername;

    /**
     * 当前mongo表的字段:字段类型map
     */
    public String mongoDocTypeMap;

    /**
     * mongo查询bson字符串
     * 举例:{"username": "Tom"}
     */
    public String mongoQueryCondition;

    /**
     * mongo指定返回字段
     * 举例:{"_id": 1, "username": 1, "product": 1, "price": 1, "type": 1}
     */
    public String mongoNeededFileds;

    /**
     * 对应的mongodb集合名称
     */
    public String mongoCollectionName;



}
