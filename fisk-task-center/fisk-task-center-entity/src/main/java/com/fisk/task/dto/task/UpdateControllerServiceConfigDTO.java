package com.fisk.task.dto.task;

import com.fisk.task.dto.nifi.BuildDbControllerServiceDTO;
import lombok.Data;

/**
 * 修改ControllerService配置信息DTO
 */
@Data
public class UpdateControllerServiceConfigDTO {
    /**
     * 组件id
     */
    private String componentId;

    /**
     * 组件name
     */
    private String componentName;

    /**
     * datasource-config
     */
    private BuildDbControllerServiceDTO dbControllerServiceDTO;
}
