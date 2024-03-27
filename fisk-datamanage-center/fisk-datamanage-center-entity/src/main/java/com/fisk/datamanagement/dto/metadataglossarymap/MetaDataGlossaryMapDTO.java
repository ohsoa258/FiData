package com.fisk.datamanagement.dto.metadataglossarymap;

import com.fisk.datamanagement.enums.EntityTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MetaDataGlossaryMapDTO {

    @ApiModelProperty(value = "元数据实体ID")
    public Integer metadataEntityId;

    @ApiModelProperty(value = "术语ID")
    public Integer glossaryId;

    @ApiModelProperty(value = "术语名称")
    public String glossaryName;

    @ApiModelProperty(value = "type_id")
    public EntityTypeEnum typeId;
}
