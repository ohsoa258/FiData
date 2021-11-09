package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildRedisDistributedMapCacheClientServiceDTO  extends BaseProcessorDTO {
    public boolean enabled;
    public String redisConnectionPool;
}
