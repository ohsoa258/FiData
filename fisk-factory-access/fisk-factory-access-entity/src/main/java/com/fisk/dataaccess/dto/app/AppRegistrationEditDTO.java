package com.fisk.dataaccess.dto.app;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 *
 * @author Lock
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppRegistrationEditDTO extends AppRegistrationDTO {

    @NotNull(message = "id不可为null")
    @ApiModelProperty(value = "主键", required = true)
    public long id;
}

