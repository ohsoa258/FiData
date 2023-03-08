package com.fisk.datamanagement.dto.metadataentity;

import com.fisk.datamanagement.dto.lineagemaprelation.LineageMapRelationDTO;
import lombok.Data;

import java.util.List;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-03 17:23
 * @description
 */
@Data
public class MetadataEntityDTO {
    public long id;

    public String name;

    public String displayName;

    public String owner;

    public String description;

    public Integer typeId;

    public Integer parentId;

    public String qualifiedName;

    public List<LineageMapRelationDTO> relationDTOList;
}
