package com.fisk.datamanagement.dto.process;

import lombok.Data;
import org.omg.CORBA.PUBLIC_MEMBER;

/**
 * @author JianWenYang
 */
@Data
    public class ProcessRelationshipAttributesPutDTO {
    public String guid;
    public String typeName;
    public String entityStatus;
    public String displayText;
    public String relationshipType;
    public String relationshipGuid;
    public String relationshipStatus;
    public ProcessRelationShipAttributesTypeNameDTO relationshipAttributes;
}
