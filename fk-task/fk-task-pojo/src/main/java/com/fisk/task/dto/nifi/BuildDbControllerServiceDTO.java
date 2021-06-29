package com.fisk.task.dto.nifi;

import com.davis.client.model.PositionDTO;
import lombok.Data;

/**
 * @author gy
 */
@Data
public class BuildDbControllerServiceDTO {
    public String groupId;
    public String name;
    public String details;
    public PositionDTO positionDTO;
    public boolean enabled;
    public String conUrl;
    public String driverName;
    public String driverLocation;
    public String user;
    public String pwd;
}
