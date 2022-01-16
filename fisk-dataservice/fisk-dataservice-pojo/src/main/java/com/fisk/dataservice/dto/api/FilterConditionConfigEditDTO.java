package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;

/**
 * @author dick
 * @version v1.0
 * @description 过滤条件编辑DTO
 * @date 2022/1/15 16:45
 */
@EqualsAndHashCode(callSuper = true)
public class FilterConditionConfigEditDTO extends FilterConditionConfigDTO {
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;
}
