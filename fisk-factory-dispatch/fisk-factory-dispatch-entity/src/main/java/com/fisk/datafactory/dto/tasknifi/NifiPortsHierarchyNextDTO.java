package com.fisk.datafactory.dto.tasknifi;

import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 * @version 1.3
 * @description
 * @date 2022/2/16 15:30
 */
@Data
public class NifiPortsHierarchyNextDTO {
    /**
     * NifiCustomWorkflowDetailDTO
     */
    @ApiModelProperty(value = "当前组件属性")
    public Long itselfPort;

    /**
     * 上一级组件集合
     */
    public List<Long> upPortList;
}
