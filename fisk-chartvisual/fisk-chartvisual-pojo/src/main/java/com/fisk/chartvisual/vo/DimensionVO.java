package com.fisk.chartvisual.vo;

import java.util.List;

/**
 * @author JinXingWang
 */
public class DimensionVO {
    public String Name;
    public String UniqueName;
    /**
     * 2 维度 3 度量
     */
    public int DimensionType;
    public List<HierarchyVO> children;
}
