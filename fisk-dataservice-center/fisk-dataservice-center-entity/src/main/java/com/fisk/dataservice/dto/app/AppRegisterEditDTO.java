package com.fisk.dataservice.dto.app;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description 应用编辑 DTO
 * @date 2022/1/6 14:51
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppRegisterEditDTO extends AppRegisterDTO
{
    /**
     * 应用id
     */
    @ApiModelProperty(value = "应用id")
    @NotNull()
    public Integer id;
}

