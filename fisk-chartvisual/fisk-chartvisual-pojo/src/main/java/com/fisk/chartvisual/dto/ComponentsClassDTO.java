package com.fisk.chartvisual.dto;

import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/2/9 15:32
 */
@Data
public class ComponentsClassDTO {

    private Integer id;
    private Integer pid;
    private String name;
    private String icon;
    private List<ComponentsClassDTO> children;

    public ComponentsClassDTO(Integer id,Integer pid, String name, String icon) {
        this.id = id;
        this.pid = pid;
        this.name = name;
        this.icon = icon;
    }

    public ComponentsClassDTO() {
    }
}
