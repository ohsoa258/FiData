package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author Lock
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_table_syncmode")
public class TableSyncmodePO extends BaseEntity implements Serializable {

    /**
     * 注入的是tb_table_access表的id
     */
    @TableId(value = "id",type = IdType.INPUT)
    public long id;

    /**
     * 1：全量、2：时间戳增量、3：业务时间覆盖、4：自定义覆盖；
     */
    public Integer syncMode;

    /**
     * 时间戳字段
     */
    public String syncField;

    /**
     * 自定义删除条件：定义每次同步的时候删除我们已有的数据条件
     */
    public String customDeleteCondition;

    /**
     * 自定义插入条件：定义删除之后获取插入条件的数据进行插入
     */
    public String customInsertCondition;

    /**
     * timer driver
     */
    public String timerDriver;

    /**
     * corn表达式
     */
    public String cornExpression;

    /**
     * 保留历史数据 0 不保留历史版本 1 保留历史版本
     */
    public int retainHistoryData;

    /**
     * 保留时间
     */
    public  int retainTime;

    /**
     * 保留单位 年/季/月/周/日
     */
    public String retainUnit;

    /**
     * 版本单位 年/季/月/周/日/自定义
     */
    public String versionUnit;

    /**
     * 版本自定义规则
     */
    public String versionCustomRule;

    /**
     * 单个数据流文件加载最大数据行
     */
    public Integer maxRowsPerFlowFile;

    /**
     * 单词从结果集中提取的最大数据行
     */
    public Integer fetchSize;

}
