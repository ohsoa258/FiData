package com.fisk.task.dto.nifi;

import com.davis.client.model.PositionDTO;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class BuildProcessGroupDTO {
    public String pid;
    public String name;
    public String details;
    public PositionDTO positionDTO;
}
