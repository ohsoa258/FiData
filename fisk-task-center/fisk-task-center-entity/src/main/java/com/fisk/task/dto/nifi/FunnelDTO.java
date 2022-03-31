package com.fisk.task.dto.nifi;

import com.davis.client.model.FunnelEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FunnelDTO extends FunnelEntity {
    public String groupId;
}
