package com.fisk.chartvisual.dto.components;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author WangYan
 * @date 2022/3/21 13:17
 */
@Data
public class SaveComponentsOptionDTO {

    @NotNull
    private Integer id;
    private Integer componentId;
    private String description;
    private String version;
    private String path;
    private String fileName;
}
