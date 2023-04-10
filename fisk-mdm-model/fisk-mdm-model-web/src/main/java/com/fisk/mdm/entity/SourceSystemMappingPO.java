package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author JinXingWang
 */
@TableName("tb_source_system_mapping")
@Data
public class SourceSystemMappingPO extends BasePO {
    public long matchingRulesId;
    public Integer sourceSystemId;
    public Integer sourceSystemTableId;
}
