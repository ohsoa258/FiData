package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import com.fisk.dataaccess.enums.ScanStartupModeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Lock
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_table_access")
public class TableAccessPO extends BasePO {

    @TableId(value = "id", type = IdType.AUTO)
    public long id;

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
     * 0: 未发布  1: 发布成功  2: 发布失败  3: 正在发布
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

}
