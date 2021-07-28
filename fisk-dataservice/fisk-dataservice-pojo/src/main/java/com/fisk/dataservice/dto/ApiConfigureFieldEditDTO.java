package com.fisk.dataservice.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author WangYan
 * @date 2021/7/28 10:26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApiConfigureFieldEditDTO extends ApiConfigureField{

    @NotNull(message = "id不可为null")
    public Integer id;
}
