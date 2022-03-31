package com.fisk.chartvisual.dto.components;

import lombok.Data;

import java.util.List;

/**
 * @author WangYan
 * @date 2022/2/9 15:32
 */
@Data
public class ComponentsDTO {

    private Integer id;
    private Integer classId;
    private String name;
    private String icon;
    private List<ComponentsOptionDTO> optionList;
}
