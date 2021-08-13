package com.fisk.chartvisual.vo;

import com.fisk.chartvisual.enums.DimensionTypeEnum;
import lombok.Data;

/**
 * @author JinXingWang
 */
@Data
public class HierarchyVO {
    public String name;
    public String uniqueName;
    public DimensionTypeEnum dimensionType;
}
