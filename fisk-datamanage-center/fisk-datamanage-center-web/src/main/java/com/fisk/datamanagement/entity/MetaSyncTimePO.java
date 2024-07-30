package com.fisk.datamanagement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @TableName tb_meta_sync_time
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "tb_meta_sync_time")
@Data
public class MetaSyncTimePO extends BasePO implements Serializable {

    /**
     * 服务类型：
     * (1, 数据接入),
     * (2, 数仓建模)，
     * (3, API网关服务),
     * (4, 数据库分发服务),
     * (5, 数据分析试图服务),
     * (6, 主数据),
     * (7,外部数据源)
     */
    private Integer serviceType;

    /**
     * 完成状态（1成功，2失败）
     */
    private Integer status;

    /**
     * 元数据总数
     */
    private Integer TotalNum;

}