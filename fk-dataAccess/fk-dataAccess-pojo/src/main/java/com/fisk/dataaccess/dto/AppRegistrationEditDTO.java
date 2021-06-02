package com.fisk.dataaccess.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 *
 * @author: Lock
 * @data: 2021/5/31 15:56
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppRegistrationEditDTO extends AppRegistrationDTO {

    @NotNull(message = "id不可为null")
    public String id;
}

