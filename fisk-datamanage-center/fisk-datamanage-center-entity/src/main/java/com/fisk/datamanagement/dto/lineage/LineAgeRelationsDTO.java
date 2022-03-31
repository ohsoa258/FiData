package com.fisk.datamanagement.dto.lineage;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class LineAgeRelationsDTO {

    public String fromEntityId;

    public String toEntityId;

    public String relationshipId;

}
