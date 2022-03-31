package com.fisk.datamanagement.dto.process;

import lombok.Data;

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
