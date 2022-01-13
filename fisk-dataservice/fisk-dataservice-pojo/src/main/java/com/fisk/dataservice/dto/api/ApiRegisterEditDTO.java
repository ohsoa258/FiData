package com.fisk.dataservice.dto.api;

import com.fisk.dataservice.dto.app.AppRegisterDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author dick
 * @version v1.0
 * @description api编辑 DTO
 * @date 2022/1/6 14:51
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApiRegisterEditDTO extends AppRegisterDTO {
    /**
     * apiId
     */
    @ApiModelProperty(value = "apiId")
    @NotNull()
    public Integer apiId;
}
