package com.fisk.datagovernance.dto.datasecurity.columnsecurityconfig;

import com.fisk.datagovernance.dto.datasecurity.columnuserassignment.ColumnUserAssignmentDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ColumnSecurityConfigUserAssignmentDTO extends ColumnSecurityConfigDTO {

    @ApiModelProperty(value = "创建用户")
    public String createUser;

    @ApiModelProperty(value = "DTO列表赋值")
    public List<ColumnUserAssignmentDTO> assignmentDtoList;

}
