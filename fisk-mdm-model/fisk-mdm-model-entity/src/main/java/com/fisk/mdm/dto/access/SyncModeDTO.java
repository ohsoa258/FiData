package com.fisk.mdm.dto.access;

import lombok.Data;

/**
 * @author jianwenyang
 */
@Data
public class SyncModeDTO {
    public long id;
    /**
     * 同步表id
     */
    public int syncTableId;
    /**
     * 同步方式；1：追加、2：全量、3：业务主键覆盖、4：业务时间覆盖；
     * 追加：每次同步的数据将以插入的方式追加的原来的表中；
     * 全量：每次同步时系统将清空原来的表，并重新插入；
     * 业务主键覆盖：系统将根据当前设定的业务主键进行覆盖；
     * 业务时间覆盖：系统将根据以下选择的条件先从表中删除满足条件的数据，然后重新插入满足条件的数据
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
