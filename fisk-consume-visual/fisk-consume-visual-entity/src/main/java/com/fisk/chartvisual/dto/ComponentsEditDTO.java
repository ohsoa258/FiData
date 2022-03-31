package com.fisk.chartvisual.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author WangYan
 * @date 2022/2/11 11:38
 */
@Data
public class ComponentsEditDTO extends ComponentsDTO{

    @NotNull
    private Integer id;
}
