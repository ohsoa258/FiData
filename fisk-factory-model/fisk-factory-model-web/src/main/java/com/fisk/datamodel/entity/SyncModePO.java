package com.fisk.datamodel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_sync_mode")
@EqualsAndHashCode(callSuper = true)
public class SyncModePO extends BasePO {
    /**
     * 同步表id
     */
    public int syncTableId;
    /**
     * 表类型:0维度表 1事实表
     */
    public int tableType;
    /**
     * 同步方式；1：全量、2：追加、3：业务时间覆盖、4：自定义覆盖；
     * <p>
     * 全量：每次都全量覆盖整张表；
     * <p>
     * 时间戳增量：根据表字段中配置的业务主键和时间戳字段进行增量覆盖；
     * <p>
     * 业务时间覆盖：根据配置的业务时间字段进行业务时间覆盖：
     */
    public int syncMode;
    /**
     * 单个数据流文件加载最大数据行
     */
    public Integer maxRowsPerFlowFile;

    /**
     * 单词从结果集中提取的最大数据行
     */
    public Integer fetchSize;

}
