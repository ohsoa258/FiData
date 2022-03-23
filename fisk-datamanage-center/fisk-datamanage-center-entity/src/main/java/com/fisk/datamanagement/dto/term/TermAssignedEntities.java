package com.fisk.datamanagement.dto.term;

import com.fisk.datamanagement.dto.entity.EntityGuidDTO;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class TermAssignedEntities {

    public String termGuid;

    public List<EntityGuidDTO> dto;

}
