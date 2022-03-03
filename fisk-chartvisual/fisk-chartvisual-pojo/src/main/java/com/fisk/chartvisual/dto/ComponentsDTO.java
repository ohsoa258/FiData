package com.fisk.chartvisual.dto;

import lombok.Data;

/**
 * @author WangYan
 * @date 2022/2/9 15:32
 */
@Data
public class ComponentsDTO {

    private Integer id;
    private Integer classId;
    private String name;
    private String description;
    private String version;
    private String icon;
    private String path;
}
