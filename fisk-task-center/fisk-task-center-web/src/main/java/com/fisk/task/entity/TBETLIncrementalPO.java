package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: DennyHui
 * CreateTime: 2021/9/3 00:43
 * Description:
 */
@Data
@TableName("tb_etl_Incremental")
public class TBETLIncrementalPO implements Serializable {
    @TableId(type = IdType.AUTO)
    public int id;
    /**
     * 数据同步流程的表名
     */
    public String objectName;

    /**
     * 同步流程是否启动,1启动  2停止
     */
    public String enableFlag;

    /**
     * 预留字段；数据同步批次号
     */
    public String incrementalObjectivescoreBatchno;

    /**
     * 最近一次同步的数据的时间范围的开始时间
     */
    public Date incrementalObjectivescoreStart;

    /**
     * 最近一次同步的数据的时间范围的结束时间
     */
    public Date incrementalObjectivescoreEnd;
}
