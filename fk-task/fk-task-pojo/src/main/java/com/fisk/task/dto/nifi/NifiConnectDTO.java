package com.fisk.task.dto.nifi;

import com.davis.client.model.ConnectableDTO;
import lombok.Data;

/**
 * @author Lock
 */
@Data
public class NifiConnectDTO {

    public String groupId;
    public String id;
    public ConnectableDTO.TypeEnum typeEnum;
}
