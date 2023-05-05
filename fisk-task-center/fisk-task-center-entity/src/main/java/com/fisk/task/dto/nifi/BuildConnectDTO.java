package com.fisk.task.dto.nifi;

import com.davis.client.model.ConnectionDTO;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "当前组件在哪个组下的组件id")
    public String fatherComponentId;
    /**
     * 目标组件对象
     */
    @ApiModelProperty(value = "目标组件对象")
    public NifiConnectDTO destination;
    /**
     * 源组件对象
     */
    @ApiModelProperty(value = "源组件对象")
    public NifiConnectDTO source;
    /**
     * 区别不同port连接线封装不同的对象
     */
    @ApiModelProperty(value = "区别不同port连接线封装不同的对象")
    public int level;

    @ApiModelProperty(value = "负载均衡策略枚举")
    public ConnectionDTO.LoadBalanceStrategyEnum loadBalanceStrategyEnum;
}
