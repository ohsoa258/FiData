package com.fisk.datamanagement.dto.process;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class ProcessRelationshipAttributesDTO {

    public List<ProcessRelationshipAttributesPutDTO> outputs;

    public List<ProcessRelationshipAttributesPutDTO> inputs;

}
