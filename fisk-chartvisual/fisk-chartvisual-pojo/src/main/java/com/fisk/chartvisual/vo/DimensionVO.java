package com.fisk.chartvisual.vo;

import com.fisk.chartvisual.enums.DimensionTypeEnum;

import java.util.List;

/**
 * @author JinXingWang
 */
public class DimensionVO {
    public String name;
    public String uniqueName;
    public DimensionTypeEnum dimensionType;
    public List<HierarchyVO> children;
}
