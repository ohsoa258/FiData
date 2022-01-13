package com.fisk.dataservice.vo.datasource;

import com.fisk.dataservice.enums.DimensionTypeEnum;

import java.util.List;

/**
 * @author dick
 * @version v1.0
 * @description DimensionVO
 * @date 2022/1/6 14:51
 */
public class DimensionVO {
    public String name;
    public String uniqueName;
    public DimensionTypeEnum dimensionType;
    public List<HierarchyVO> children;
}
