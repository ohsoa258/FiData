package com.fisk.task.dto.nifi;

import com.davis.client.model.PositionDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author gy
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BuildDbControllerServiceDTO extends BaseProcessorDTO {
    public boolean enabled;
    public String conUrl;
    public String driverName;
    public String driverLocation;
    public String user;
    public String pwd;
}
