package com.fisk.datamanagement.dto.relationship;

import com.fisk.datamanagement.dto.process.ProcessAttributesPutDTO;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class RelationshipDTO {
    public String guid;
    public String typeName;
    public ProcessAttributesPutDTO end1;
    public ProcessAttributesPutDTO end2;
}
