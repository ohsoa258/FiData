package com.fisk.datafactory.dto.tasknifi;

import com.fisk.datafactory.dto.customworkflowdetail.NifiCustomWorkflowDetailDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lock
 * @version 1.3
 * @description 组件层级关系对象
 * @date 2022/2/16 15:26
 */
@Data
public class NifiPortsHierarchyDTO {

    @ApiModelProperty(value = "当前组件属性")
    public NifiCustomWorkflowDetailDTO itselfPort;

    /**
     * 下一级组件集合
     */
    public List<NifiPortsHierarchyNextDTO> nextList;
}
