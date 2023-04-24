package com.fisk.chartvisual.dto.components;

import com.fisk.chartvisual.enums.ComponentsTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/2/9 15:32
 */
@Data
public class ComponentsClassDTO {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "pid")
    private Integer pid;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "图标")
    private String icon;

    @ApiModelProperty(value = "类型")
    private ComponentsTypeEnum type;

    @ApiModelProperty(value = "子类")
    private List<ComponentsClassDTO> children;

    @ApiModelProperty(value = "组成DTO列表")
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
