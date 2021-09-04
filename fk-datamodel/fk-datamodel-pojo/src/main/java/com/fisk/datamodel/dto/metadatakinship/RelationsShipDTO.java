package com.fisk.datamodel.dto.metadatakinship;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class RelationsShipDTO {
    public List<String> guidList;
    public List<RelationsDTO> list;
}
