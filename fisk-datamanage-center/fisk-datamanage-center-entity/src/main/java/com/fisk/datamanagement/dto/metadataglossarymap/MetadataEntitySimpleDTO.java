package com.fisk.datamanagement.dto.metadataglossarymap;

import com.fisk.datamanagement.dto.lineagemaprelation.LineageMapRelationDTO;
import com.fisk.datamanagement.enums.EntityTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author zjy
 * @version 1.0
 * @createTime 2023-03-03 17:23
 * @description
 */
@Data
public class MetadataEntitySimpleDTO {
    @ApiModelProperty(value = "id")
    public long id;

    @ApiModelProperty(value = "类型id")
    public EntityTypeEnum typeId;

}
