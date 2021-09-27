package com.fisk.task.dto.nifi;

import lombok.Data;

/**
 * <p>
 * 创建input_port or output_port
 * </p>
 *
 * @author Lock
 */
@Data
public class BuildPortDTO {

    /**
     * 当前input_port/output_port唯一标识
     */
    public String clientId;
    /**
     * 名称
     */
    public String portName;
    /**
     * 上级组件id
     */
    public String componentId;

}
