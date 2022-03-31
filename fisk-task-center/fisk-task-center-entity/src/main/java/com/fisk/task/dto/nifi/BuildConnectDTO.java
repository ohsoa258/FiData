package com.fisk.task.dto.nifi;

import com.davis.client.model.ConnectionDTO;
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
     * 当前组件在哪个组下的组件id
     */
    public String fatherComponentId;
    /**
     * 目标组件对象
     */
    public NifiConnectDTO destination;
    /**
     * 源组件对象
     */
    public NifiConnectDTO source;
    /**
     * 区别不同port连接线封装不同的对象
     */
    public int level;

    public ConnectionDTO.LoadBalanceStrategyEnum loadBalanceStrategyEnum;
}
