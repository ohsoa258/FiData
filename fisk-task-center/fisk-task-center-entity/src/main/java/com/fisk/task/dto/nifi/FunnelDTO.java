package com.fisk.task.dto.nifi;

import com.davis.client.model.FunnelEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cfk
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FunnelDTO extends FunnelEntity {
    @ApiModelProperty(value = "组Id")
    public String groupId;
}
