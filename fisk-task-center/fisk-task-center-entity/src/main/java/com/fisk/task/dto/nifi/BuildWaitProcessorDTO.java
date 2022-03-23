package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildWaitProcessorDTO  extends BaseProcessorDTO {

    public String releaseSignalIdentifier;
    public String targetSignalCount;
    public String expirationDuration;
    public String distributedCacheService;
}
