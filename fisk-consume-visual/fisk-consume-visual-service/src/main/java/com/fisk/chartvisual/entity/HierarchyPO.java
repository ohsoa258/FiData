package com.fisk.chartvisual.entity;

import com.fisk.chartvisual.enums.NodeTypeEnum;
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
    public NodeTypeEnum dimensionTypeEnum;
    public List<MemberPO> members;
}
