package com.fisk.chartvisual.vo;

import com.fisk.chartvisual.enums.NodeTypeEnum;
import lombok.Data;

/**
 * @author JinXingWang
 */
@Data
public class HierarchyVO {
    public String name;
    public String uniqueName;
    public NodeTypeEnum dimensionType;
}
