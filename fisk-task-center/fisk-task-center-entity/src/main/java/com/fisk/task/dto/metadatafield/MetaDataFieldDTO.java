package com.fisk.task.dto.metadatafield;

import com.fisk.task.dto.MQBaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-20 17:42
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetaDataFieldDTO extends MQBaseDTO {
    private Integer tableId;
    private Integer fieldId;
}
