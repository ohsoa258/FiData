package com.fisk.datamanagement.dto.process;

import lombok.Data;
import org.omg.CORBA.PUBLIC_MEMBER;

/**
 * @author JianWenYang
 */
@Data
public class ProcessAttributesPutDTO {
    public String guid;
    public String typeName;
    public String tableName;
    public ProcessUniqueAttributesDTO uniqueAttributes;
}
