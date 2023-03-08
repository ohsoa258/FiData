package com.fisk.datamanagement.dto.metadatabusinessmetadatamap;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class EditMetadataBusinessMetadataMapDTO {

    public Integer metadataEntityId;

    public List<MetadataBusinessMetadataMapDTO> list;

}
