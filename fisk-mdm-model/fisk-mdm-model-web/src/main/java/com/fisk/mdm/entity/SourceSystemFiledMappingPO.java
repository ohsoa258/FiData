package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author JinXingWang
 */
@TableName("tb_source_system_filed_mapping")
@Data
public class SourceSystemFiledMappingPO extends BasePO {
    public long sourceSystemMappingId;
    public Integer sourceSystemFiledId;
    public Integer attitudeId;
}
