package com.fisk.task.dto.nifi;

import lombok.Data;

/**
 * <p>
 * input_port or output_port连接组所需参数
 * </p>
 *
 * @author Lock
 */
@Data
public class BuildConnectDTO {

    /**
     * 指定连接组的componentId
     */
    public String componentId;

}
