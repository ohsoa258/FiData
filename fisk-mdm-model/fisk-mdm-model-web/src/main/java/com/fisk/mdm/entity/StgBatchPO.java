package com.fisk.mdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
@TableName("tb_stg_batch")
public class StgBatchPO extends BasePO {

    public int entityId;

    public int versionId;

    public String batchCode;

    public int totalCount;

    public int errorCount;

    public int status;

}
