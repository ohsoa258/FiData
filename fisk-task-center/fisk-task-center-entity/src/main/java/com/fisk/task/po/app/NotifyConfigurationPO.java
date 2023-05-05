package com.fisk.task.po.app;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("tb_notify_configuration")
public class NotifyConfigurationPO extends BasePO {
    public Integer appId;
    public Integer correlationAppId;
    public String appComponentId;
    public String correlationAppComponentId;
    public String notifyComponentId;
    public String distributedCacheServiceId;
    public String releaseSignalId;

}
