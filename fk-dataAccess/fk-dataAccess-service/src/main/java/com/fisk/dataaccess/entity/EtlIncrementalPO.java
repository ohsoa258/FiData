package com.fisk.dataaccess.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * @author Lock
 */
@Data
@TableName("tb_etl_Incremental")
public class EtlIncrementalPO implements Serializable {

    @TableId
    public long id;

    /**
     * 数据同步流程的表名
     */
    public String objectName;

    /**
     * 同步流程是否启动,1启动  2停止(默认: 1)
     */
    public int enableFlag;

    /**
     * 预留字段；数据同步批次号
     */
    public String incrementalObjectivescoreBatchno;

    /**
     * 最近一次同步的数据的时间范围的开始时间: 1970
     */
    public DateTime incrementalObjectivescoreStart;

    /**
     * 最近一次同步的数据的时间范围的结束时间
     */
    public DateTime incrementalObjectivescoreEnd;

}
