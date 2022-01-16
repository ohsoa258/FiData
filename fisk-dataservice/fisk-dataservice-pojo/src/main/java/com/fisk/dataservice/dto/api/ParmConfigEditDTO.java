package com.fisk.dataservice.dto.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author dick
 * @version v1.0
 * @description 参数编辑DTO
 * @date 2022/1/15 16:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ParmConfigEditDTO extends ParmConfigDTO{
    /**
     * id
     */
    @ApiModelProperty(value = "id")
    public int id;
}
