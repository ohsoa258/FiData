package com.fisk.dataservice.entity;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description 维度
 * @date 2022/1/6 14:51
 */
public class DimensionPO {
    public String name;
    public String uniqueName;
    public List<HierarchyPO> hierarchies;
}
