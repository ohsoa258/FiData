package com.fisk.datamanagement.dto.term;

import com.fisk.datamanagement.dto.entity.EntityGuidDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class TermAssignedEntities {

    @ApiModelProperty(value = "termGuid")
    public String termGuid;

    @ApiModelProperty(value = "dto")
    public List<EntityGuidDTO> dto;

}
