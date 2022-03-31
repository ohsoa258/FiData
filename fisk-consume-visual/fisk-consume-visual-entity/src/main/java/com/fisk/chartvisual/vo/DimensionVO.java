package com.fisk.chartvisual.vo;

import com.fisk.chartvisual.enums.NodeTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @author JinXingWang
 */
@Data
public class DimensionVO {
    public String name;
    public String uniqueName;
    public NodeTypeEnum dimensionType;
    public List<HierarchyVO> children;
}
