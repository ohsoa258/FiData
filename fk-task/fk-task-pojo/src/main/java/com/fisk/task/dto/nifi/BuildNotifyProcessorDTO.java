package com.fisk.task.dto.nifi;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildNotifyProcessorDTO  extends BaseProcessorDTO {
    public String releaseSignalIdentifier;
    public String distributedCacheService;
}
