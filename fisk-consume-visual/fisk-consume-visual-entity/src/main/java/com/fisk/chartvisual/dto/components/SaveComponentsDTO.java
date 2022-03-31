package com.fisk.chartvisual.dto.components;

import com.fisk.chartvisual.dto.components.ComponentsOptionDTO;
import lombok.Data;

/**
 * @author WangYan
 * @date 2022/2/9 15:32
 */
@Data
public class SaveComponentsDTO {

    private Integer id;
    private Integer classId;
    private String name;
    private String icon;
    private ComponentsOptionDTO option;
}
