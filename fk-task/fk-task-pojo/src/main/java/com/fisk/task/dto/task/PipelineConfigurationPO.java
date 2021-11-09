package com.fisk.task.dto.task;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("tb_pipeline_configuration")
public class PipelineConfigurationPO {
    public Integer app_id;
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
