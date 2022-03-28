package com.fisk.task.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_nifi_config")
public class NifiConfigPO extends BasePO {
    public String componentKey;
    public String componentId;

}
