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
     * 当前组件在哪个组下的组件id
     */
    public String fatherComponentId;
    /**
     * input_port组件id
     */
    public String inputPortComponentId;
    /**
     * output_port组件id
     */
    public String outputPortComponentId;
    /**
     * 连接output_port的组件 id
     */
    public String connectOutPutPortComponentId;
    /**
     * input_port将连接的组件 id
     */
    public String connectInPutPortComponentId;


}
