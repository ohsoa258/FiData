package com.fisk.task.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fisk.common.core.baseObject.entity.BasePO;
import lombok.Data;

/**
 * @author cfk
 */
@Data
@TableName("tb_pipeline_configuration")
public class PipelineConfigurationPO extends BasePO {
    public String app_id;
    public String appComponentId;
    public String inFunnelId;
    public String outFunnelId;
    public String inputPortId;
    public String outputPortId;
    public String waitComponentId;
    public String distributedCacheService;
    public String releaseSignalId;
    public Integer targetSignalCount;
    public Integer expirationDuration;
    public String redisConnectionPoolServiceId;
    public String redisDistributedMapCacheClientServiceId;
    public String connectionString;
    public String redisConnectionPool;


}
