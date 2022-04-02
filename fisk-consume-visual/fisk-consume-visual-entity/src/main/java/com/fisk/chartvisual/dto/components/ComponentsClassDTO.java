package com.fisk.chartvisual.dto.components;

import com.fisk.chartvisual.enums.ComponentsTypeEnum;
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
    private ComponentsTypeEnum type;
    private List<ComponentsClassDTO> children;
    private List<ComponentsDTO> componentsDtoList;

    public ComponentsClassDTO(Integer id,Integer pid, String name, String icon,ComponentsTypeEnum type,List<ComponentsDTO> componentsDtoList) {
        this.id = id;
        this.pid = pid;
        this.name = name;
        this.icon = icon;
        this.type = type;
        this.componentsDtoList = componentsDtoList;
    }

    public ComponentsClassDTO() {
    }
}
