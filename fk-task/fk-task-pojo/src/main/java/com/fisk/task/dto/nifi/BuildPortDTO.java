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
     * 当前input_port/output_port唯一标识(可自定义)
     */
    public String clientId;
    /**
     * 自定义名称
     */
    public String portName;
    /**
     * 属于哪个组件下的组件id
     */
    public String componentId;
    /**
     * 组件X坐标
     */
    public Double componentX;
    /**
     * 组件Y坐标
     */
    public Double componentY;

}
