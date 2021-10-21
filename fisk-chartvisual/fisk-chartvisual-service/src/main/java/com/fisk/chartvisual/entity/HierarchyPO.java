package com.fisk.chartvisual.entity;

import com.fisk.chartvisual.enums.DimensionTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * 层级
 *
 * @author JinXingWang
 */
@Data
public class HierarchyPO {
    public String name;
    public String uniqueName;
    public DimensionTypeEnum dimensionTypeEnum;
    public List<MemberPO> members;
}
