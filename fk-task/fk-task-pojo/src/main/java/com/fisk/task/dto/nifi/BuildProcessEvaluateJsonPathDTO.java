package com.fisk.task.dto.nifi;

import com.davis.client.model.PositionDTO;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class BuildProcessEvaluateJsonPathDTO {
    public String groupId;
    public String name;
    public String details;
    public PositionDTO positionDTO;
    public String fieldName;
}
