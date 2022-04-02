package com.fisk.datagovernance.dto.datasecurity.columnsecurityconfig;

import com.fisk.datagovernance.dto.datasecurity.columnuserassignment.ColumnUserAssignmentDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ColumnSecurityConfigUserAssignmentDTO extends ColumnSecurityConfigDTO {

    public String createUser;

    public List<ColumnUserAssignmentDTO> assignmentDtoList;

}
