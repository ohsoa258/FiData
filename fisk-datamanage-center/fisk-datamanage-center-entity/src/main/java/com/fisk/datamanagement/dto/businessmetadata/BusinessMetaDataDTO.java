package com.fisk.datamanagement.dto.businessmetadata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author JianWenYang
 */
@Data
public class BusinessMetaDataDTO {

    @ApiModelProperty(value = "业务员数据Defs")
    public List<BusinessMetadataDefsDTO> businessMetadataDefs;
}
