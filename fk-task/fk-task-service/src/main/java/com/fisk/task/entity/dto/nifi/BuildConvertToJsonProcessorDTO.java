package com.fisk.task.entity.dto.nifi;

import com.davis.client.model.PositionDTO;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class BuildConvertToJsonProcessorDTO {
    public String groupId;
    public String name;
    public String details;
    public PositionDTO positionDTO;
}
