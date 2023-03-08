package com.fisk.datamanagement.dto.process;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
    public class ProcessEntityDTO {

    public String typeName;

    public String guid;

    public String status;

    public ProcessAttributesDTO attributes;

    public ProcessRelationshipAttributesDTO relationshipAttributes;

}
