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
@TableName("tb_etl_log")
public class EtlLogPO implements Serializable {

    @TableId
    public long id;

    /**
     * 定义更新的表名
     */
    public String tableName;

    /**
     * 表数据开始同步时间
     */
    public DateTime startDate;

    /**
     * 表数据同步结束时间
     */
    public DateTime endDate;

    /**
     * 本次同步的数据行
     */
    public long datarows;

    /**
     * 状态；0代表正在同步，1代表同步成功，2代表同步失败
     */
    public int status;

    /**
     * 记录同步失败的原因
     */
    public String errordesc;

}
