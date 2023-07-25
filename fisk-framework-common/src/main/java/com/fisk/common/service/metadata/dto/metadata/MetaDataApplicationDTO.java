package com.fisk.common.service.metadata.dto.metadata;

import lombok.Data;

import java.util.List;

/**
 * @author JinXingWang
 */
@Data
public class MetaDataApplicationDTO extends MetaDataBaseAttributeDTO {

    /**
     * 实体
     */
    public List<MetaDataEntityDTO> entityList;
}
