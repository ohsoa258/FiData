package com.fisk.dataaccess.dto.taskschedule;

import lombok.Data;

/**
 * @author Lock
 */
@Data
public class ComponentIdDTO {
    /**
     * nifi流程回写的应用组件id
     */
    public String appComponentId;
    /**
     * nifi流程回写的物理表组件
     */
    public String tableComponentId;
}
