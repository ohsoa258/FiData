package com.fisk.common.service.metadata.dto.metadata;

import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 * @date 2022-0704 09:48
 */
@Data
public class MetaDataAttributeDTO {

    public List<MetaDataInstanceAttributeDTO> instanceList;

}
