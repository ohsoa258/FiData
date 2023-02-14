package com.fisk.datamanagement.dto.lineagemaprelation;

import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class LineageMapRelationDTO {

    public Integer metadataLineageMapId;

    public Integer fromEntityId;

    public Integer toEntityId;

}
