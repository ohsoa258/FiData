package com.fisk.mdm.dto.masterdata;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author JianWenYang
 */
@Data
public class MasterDataBaseDTO {

    /**
     * 实体id
     */
    @ApiModelProperty(value = "实体id")
    private Integer entityId;

    /**
     * 版本id
     */
    @ApiModelProperty(value = "版本id")
    private Integer versionId;

    /**
     * 模型id
     */
    @ApiModelProperty(value = "模型id")
    private Integer modelId;

}
