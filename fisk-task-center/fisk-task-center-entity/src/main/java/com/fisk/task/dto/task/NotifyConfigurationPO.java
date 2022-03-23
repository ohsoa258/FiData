package com.fisk.task.dto.task;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.entity.BasePO;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
