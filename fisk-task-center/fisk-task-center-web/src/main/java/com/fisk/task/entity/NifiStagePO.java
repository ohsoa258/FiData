package com.fisk.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;

/**
 * @author cfk
 */
@Data
@TableName("tb_nifi_stage")
public class NifiStagePO extends BasePO {
    public int componentId;
    public int queryPhase;
    public int transitionPhase;
    public int insertPhase;
    public String comment;
}
