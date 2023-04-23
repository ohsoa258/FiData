package com.fisk.mdm.dto.attribute;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author WangYan
 * @Date 2022/4/23 13:45
 * @Version 1.0
 */
@Data
public class AttributeDomainDTO {

    /**
     * 实体id
     */
    @ApiModelProperty(value = "实体id")
    private Integer entityId;
    /**
     * 业务域id
     */
    @ApiModelProperty(value = "业务域id")
    private Integer domainId;
}
