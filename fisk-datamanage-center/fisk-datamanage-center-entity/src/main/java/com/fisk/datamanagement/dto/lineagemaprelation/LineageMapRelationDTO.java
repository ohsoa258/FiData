package com.fisk.datamanagement.dto.lineagemaprelation;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class LineageMapRelationDTO {
    public Integer id;

    public Integer metadataEntityId;

    public Integer fromEntityId;

    public Integer toEntityId;

    public Integer processType;

}
